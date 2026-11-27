#!/usr/bin/env bash
# Resolves the test matrices for .github/workflows/ci.yml (ACS-13096). A workflow_call input always wins. What an empty
# input means depends on who runs the CI:
# - master (or a branch based on it) running its own ci.yml: master's default from defaults.yml (next to this script);
# - a release branch running master's ci.yml: "none", so a setting added on master after the branch was cut stays off
#   there until the branch's own caller lists it. Only java-version is required instead.
# Release branches run master's copy of this script, because ci.yml checks it out at job.workflow_sha.
#
# Reads:  INPUT_JAVA_VERSION, INPUT_DB_TESTS, INPUT_ACTIVEMQ_PROFILES, INPUT_APP_CONTEXT_TESTS, INPUT_TAS_TESTS,
#         INPUT_AGS_REST_API_SUITES (YAML or JSON; [] means "none"), EVENT_NAME, COMMIT_TITLE,
#         CI_LOGIC_REF (job.workflow_ref, e.g. owner/repo/.github/workflows/ci.yml@refs/heads/master), CODE_REF (github.ref)
# Writes: name=value lines to $GITHUB_OUTPUT (and the log)
set -euo pipefail

defaults="$(dirname "${BASH_SOURCE[0]}")/defaults.yml"
fail() { echo "::error title=Invalid CI configuration::$1"; exit 1; }

# The CI logic comes from the same ref as the code when a branch runs its own ci.yml (on a PR both are
# refs/pull/<n>/merge). A release branch calls ci.yml@master, so the two differ. Without the refs (local runs), act as master.
if [[ -z "${CI_LOGIC_REF:-}" || "${CI_LOGIC_REF}" == *"@${CODE_REF:-}" ]]; then
  use_defaults=true
  echo "Running this branch's own CI: settings that are not passed use defaults.yml"
else
  use_defaults=false
  echo "Running ${CI_LOGIC_REF##*@}'s CI for ${CODE_REF:-}: settings that are not passed mean none"
fi

# The input as compact JSON; when it is empty, the default for that key on master and [] (none) on a release branch
resolve() {
  local input="$1" key="$2"
  if [[ -n "${input//[[:space:]]/}" ]]; then
    yq -p=yaml -o=json -I=0 '.' <<<"${input}"
  elif [[ "${use_defaults}" == true ]]; then
    yq -o=json -I=0 ".[\"${key}\"]" "${defaults}"
  else
    echo "[]"
  fi
}
is_string_list='type == "array" and all(.[]; type == "string")'

java_input="${INPUT_JAVA_VERSION:-}"
if [[ -z "${java_input//[[:space:]]/}" && "${use_defaults}" != true ]]; then
  fail "java-version must be passed by a release branch's caller; it has no \"none\""
fi
java_version=$(resolve "${java_input}" java-version | jq -r '.')
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

app_context_tests=$(resolve "${INPUT_APP_CONTEXT_TESTS:-}" app-context-tests)
jq -e 'type == "array" and all(.[]; (.testSuite | type == "string") and (."compose-profile" | type == "string"))' <<<"${app_context_tests}" >/dev/null \
  || fail "app-context-tests: every entry needs a testSuite and a compose-profile"

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
