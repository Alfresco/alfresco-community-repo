#!/usr/bin/env bats

INPUTS=(INPUT_JAVA_VERSION INPUT_DB_TESTS INPUT_ACTIVEMQ_PROFILES INPUT_ELASTICSEARCH_TEST_SUITES INPUT_TAS_TESTS INPUT_AGS_REST_API_SUITES)
SETTINGS=(java-version db-tests activemq-profiles elasticsearch-test-suites tas-tests ags-rest-api-suites)

setup() {
    CI_DIR="$( cd "$( dirname "$BATS_TEST_FILENAME" )/.." >/dev/null 2>&1 && pwd )"
    RESOLVE="$CI_DIR/resolve-ci-config.sh"

    export GITHUB_OUTPUT="$BATS_TEST_TMPDIR/github_output"
    : > "$GITHUB_OUTPUT"
    export EVENT_NAME="push"
    export COMMIT_TITLE="ACS-123 some change"
    unset "${INPUTS[@]}"
}

# helper: a named output written to $GITHUB_OUTPUT
get_output() {
    grep "^$1=" "$GITHUB_OUTPUT" | cut -d= -f2-
}

# helper: a jq query on a named output, one result per line
query() {
    get_output "$1" | jq -r "$2"
}

# helper: a scratch git repository on the given branch, holding a copy of .github/ci and a placeholder ci.yml
make_repo() {
    REPO="$BATS_TEST_TMPDIR/repo"
    mkdir -p "$REPO/.github/workflows"
    cp -r "$CI_DIR" "$REPO/.github/ci"
    echo "name: placeholder for master's full workflow" > "$REPO/.github/workflows/ci.yml"
    git init -q -b "$1" "$REPO"
}

# --- resolve-ci-config.sh ---------------------------------------------------------------------------

@test "no inputs: master's defaults from defaults.yml" {
    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    [ "$(get_output java-version)" = "21" ]
    [ "$(query db-tests 'length')" = "6" ]
    [ "$(get_output activemq-profiles)" = '["activemq6","activemq5"]' ]
    [ "$(query tas-tests 'length')" = "16" ]
    [ "$(query tas-tests 'any(.[]; ."test-name" == "REST API TAS tests part4")')" = "true" ]
    [ "$(query app-context-tests '[.[] | select(."compose-profile" == "with-elasticsearch") | .testSuite] | join(",")')" = \
      "ElasticsearchCommunityTestsPart1TestSuite,ElasticsearchCommunityTestsPart2TestSuite,ElasticsearchCommunityTestsPart3TestSuite" ]
    [ "$(get_output ags-rest-api-suites)" = '[{"name":"part1","suite-file":"testng-part1.xml"},{"name":"part2","suite-file":"testng-part2.xml"}]' ]
}

@test "inputs replace the defaults (a release branch's settings)" {
    export INPUT_JAVA_VERSION="17"
    export INPUT_ELASTICSEARCH_TEST_SUITES="- ElasticsearchCommunityTestsTestSuite"
    export INPUT_TAS_TESTS='
- { test-name: "REST API TAS tests part1", pom-dir: tas-restapi, test-profile: run-restapi-part1 }
- { test-name: "Email TAS tests", pom-dir: tas-email }'
    export INPUT_AGS_REST_API_SUITES="[testng.xml]"

    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    [ "$(get_output java-version)" = "17" ]
    [ "$(query tas-tests '[.[]."test-name"] | join(",")')" = "REST API TAS tests part1,Email TAS tests" ]
    [ "$(query app-context-tests '[.[] | select(."compose-profile" == "with-elasticsearch") | .testSuite] | join(",")')" = "ElasticsearchCommunityTestsTestSuite" ]
    [ "$(get_output ags-rest-api-suites)" = '[{"name":"all","suite-file":"testng.xml"}]' ]
}

@test "an input that is not passed keeps master's default" {
    export INPUT_TAS_TESTS="- { test-name: Email TAS tests, pom-dir: tas-email }"

    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    [ "$(query tas-tests 'length')" = "1" ]
    [ "$(query db-tests 'length')" = "6" ]
}

@test "a PR without [db] in its title drops the pr-gated DB tests" {
    export EVENT_NAME="pull_request"
    export COMMIT_TITLE="ACS-123 fix something"

    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    [ "$(query db-tests '[.[].name] | join(",")')" = "MariaDB 11.8,MySQL 8,PostgreSQL 17.9" ]
}

@test "a PR with [DB] in its title keeps every DB test (case-insensitive)" {
    export EVENT_NAME="pull_request"
    export COMMIT_TITLE="ACS-123 fix something [DB]"

    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    [ "$(query db-tests 'length')" = "6" ]
}

@test "[] means none, and JSON is accepted as well as YAML" {
    export INPUT_ELASTICSEARCH_TEST_SUITES="[]"
    export INPUT_ACTIVEMQ_PROFILES='["activemq"]'

    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    [ "$(query app-context-tests '[.[] | select(."compose-profile" == "with-elasticsearch")] | length')" = "0" ]
    [ "$(get_output activemq-profiles)" = '["activemq"]' ]
}

@test "an unquoted DB version is rejected (YAML would turn 15.10 into 15.1)" {
    export INPUT_DB_TESTS="- { name: PostgreSQL 15.10, profile: postgres, version: 15.10 }"

    run bash "$RESOLVE"

    [ "$status" -ne 0 ]
    [[ "$output" == *"quoted version"* ]]
    [ ! -s "$GITHUB_OUTPUT" ]
}

@test "an unknown DB profile is rejected" {
    export INPUT_DB_TESTS='- { name: Oracle 19, profile: oracle, version: "19" }'

    run bash "$RESOLVE"

    [ "$status" -ne 0 ]
    [[ "$output" == *"mariadb, mysql or postgres"* ]]
}

@test "a TAS entry without pom-dir is rejected" {
    export INPUT_TAS_TESTS="- { test-name: Email TAS tests }"

    run bash "$RESOLVE"

    [ "$status" -ne 0 ]
    [[ "$output" == *"pom-dir"* ]]
}

@test "a java-version that is not a number is rejected" {
    export INPUT_JAVA_VERSION="latest"

    run bash "$RESOLVE"

    [ "$status" -ne 0 ]
    [[ "$output" == *"java-version"* ]]
}

@test "settings that look like they hold a password are rejected" {
    export INPUT_TAS_TESTS='- { test-name: Email TAS tests, pom-dir: tas-email, mvn-options: "-Dadmin.password=secret" }' # pragma: allowlist secret

    run bash "$RESOLVE"

    [ "$status" -ne 0 ]
    [[ "$output" == *"credentials"* ]]
}

# --- create-release-caller.sh -----------------------------------------------------------------------

@test "cut script refuses to run on a branch that is not release/*" {
    make_repo "feature/something"
    cd "$REPO"

    run bash .github/ci/create-release-caller.sh

    [ "$status" -ne 0 ]
    [[ "$output" == *"release/*"* ]]
    [ "$(cat .github/workflows/ci.yml)" = "name: placeholder for master's full workflow" ]
}

@test "cut script writes a caller of ci.yml@master with every setting and the secrets" {
    make_repo "release/26.3"
    cd "$REPO"

    run bash .github/ci/create-release-caller.sh

    [ "$status" -eq 0 ]
    [ "$(yq '.jobs.ci.uses' .github/workflows/ci.yml)" = "Alfresco/alfresco-community-repo/.github/workflows/ci.yml@master" ]
    [ "$(yq '.jobs.ci.with | keys | join(",")' .github/workflows/ci.yml)" = "$(IFS=,; echo "${SETTINGS[*]}")" ]
    [ "$(yq '.on.workflow_call.secrets | keys | length' .github/workflows/ci.yml)" = "10" ]
    [ "$(yq '.jobs.ci.permissions."id-token"' .github/workflows/ci.yml)" = "write" ]
}

@test "the caller written at the cut resolves exactly like master's defaults" {
    make_repo "release/26.3"
    (cd "$REPO" && bash .github/ci/create-release-caller.sh)

    run bash "$RESOLVE"
    [ "$status" -eq 0 ]
    cp "$GITHUB_OUTPUT" "$BATS_TEST_TMPDIR/defaults_output"
    : > "$GITHUB_OUTPUT"

    for i in "${!SETTINGS[@]}"; do
        export "${INPUTS[$i]}=$(yq ".jobs.ci.with.\"${SETTINGS[$i]}\"" "$REPO/.github/workflows/ci.yml")"
    done
    run bash "$RESOLVE"

    [ "$status" -eq 0 ]
    diff "$BATS_TEST_TMPDIR/defaults_output" "$GITHUB_OUTPUT"
}
