#!/usr/bin/env bash
# Resolves the test matrices for .github/workflows/ci.yml (ACS-13096): a workflow_call input wins, an empty input falls
# back to master's default in defaults.yml (next to this script). Release branches run master's copy of this script,
# because ci.yml checks it out at job.workflow_sha rather than taking it from the calling branch.
#
# Reads:  INPUT_JAVA_VERSION, INPUT_DB_TESTS, INPUT_ACTIVEMQ_PROFILES, INPUT_ELASTICSEARCH_TEST_SUITES, INPUT_TAS_TESTS,
#         INPUT_AGS_REST_API_SUITES (YAML or JSON; [] means "none"), EVENT_NAME, COMMIT_TITLE
# Writes: name=value lines to $GITHUB_OUTPUT (and the log)
set -euo pipefail

defaults="$(dirname "${BASH_SOURCE[0]}")/defaults.yml"
fail() { echo "::error title=Invalid CI configuration::$1"; exit 1; }

# The input as compact JSON, or the default for that key when the input is empty
resolve() {
  local input="$1" key="$2"
  if [[ -n "${input//[[:space:]]/}" ]]; then
    yq -p=yaml -o=json -I=0 '.' <<<"${input}"
  else
    yq -o=json -I=0 ".[\"${key}\"]" "${defaults}"
  fi
}
is_string_list='type == "array" and all(.[]; type == "string")'

java_version=$(resolve "${INPUT_JAVA_VERSION:-}" java-version | jq -r '.')
[[ "${java_version}" =~ ^[0-9]+$ ]] || fail "java-version must be a number, for example \"21\""

db_tests=$(resolve "${INPUT_DB_TESTS:-}" db-tests)
jq -e 'type == "array" and all(.[]; (.name | type == "string") and (.profile | IN("mariadb", "mysql", "postgres")) and (.version | type == "string"))' <<<"${db_tests}" >/dev/null \
  || fail "db-tests: every entry needs a name, a profile (mariadb, mysql or postgres) and a quoted version"
# pr-gated DB tests run on PRs only when the title contains [db] (case-insensitive, like contains())
shopt -s nocasematch
if [[ "${EVENT_NAME:-}" == "pull_request" && "${COMMIT_TITLE:-}" != *"[db]"* ]]; then
  db_tests=$(jq -c '[.[] | select(."pr-gated" != true)]' <<<"${db_tests}")
fi
shopt -u nocasematch

activemq_profiles=$(resolve "${INPUT_ACTIVEMQ_PROFILES:-}" activemq-profiles)
jq -e "${is_string_list}" <<<"${activemq_profiles}" >/dev/null || fail "activemq-profiles must be a list of profile names"

es_suites=$(resolve "${INPUT_ELASTICSEARCH_TEST_SUITES:-}" elasticsearch-test-suites)
jq -e "${is_string_list}" <<<"${es_suites}" >/dev/null || fail "elasticsearch-test-suites must be a list of test suite names"
app_context_tests=$(yq -o=json -I=0 '.["app-context-tests"]' "${defaults}" | jq -c --argjson es "${es_suites}" \
  '. + [$es[] | {testSuite: ., "compose-profile": "with-elasticsearch", "mvn-options": "-Dindex.subsystem.name=elasticsearch"}]')

tas_tests=$(resolve "${INPUT_TAS_TESTS:-}" tas-tests)
jq -e 'type == "array" and all(.[]; (."test-name" | type == "string") and (."pom-dir" | type == "string"))' <<<"${tas_tests}" >/dev/null \
  || fail "tas-tests: every entry needs a test-name and a pom-dir"

ags_suites=$(resolve "${INPUT_AGS_REST_API_SUITES:-}" ags-rest-api-suites)
jq -e "${is_string_list}" <<<"${ags_suites}" >/dev/null || fail "ags-rest-api-suites must be a list of TestNG suite files"
# testng-part1.xml -> part1 (keeps the job names and S3 paths); testng.xml -> all
ags_suites=$(jq -c '[.[] | {name: (rtrimstr(".xml") | ltrimstr("testng") | ltrimstr("-") | if . == "" then "all" else . end), "suite-file": .}]' <<<"${ags_suites}")

# Job outputs must not look like they hold a password, or GitHub drops them
for value in "${db_tests}" "${activemq_profiles}" "${app_context_tests}" "${tas_tests}" "${ags_suites}"; do
  [[ "${value,,}" != *password* ]] || fail "keep credentials out of the test lists; pass them in the job step instead"
done

{
  echo "java-version=${java_version}"
  echo "db-tests=${db_tests}"
  echo "activemq-profiles=${activemq_profiles}"
  echo "app-context-tests=${app_context_tests}"
  echo "tas-tests=${tas_tests}"
  echo "ags-rest-api-suites=${ags_suites}"
} | tee -a "${GITHUB_OUTPUT:-/dev/null}"
