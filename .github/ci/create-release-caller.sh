#!/usr/bin/env bash
# Run once on a newly cut release branch (ACS-13096), from the repository root:
#
#   bash .github/ci/create-release-caller.sh
#
# It replaces .github/workflows/ci.yml, master's full workflow, with a caller of master's ci.yml that passes every
# setting explicitly, copied from defaults.yml as it is at the cut. Master's later default changes then never reach
# the branch; its settings change only in its own caller. Review the result, then commit it.
set -euo pipefail

dir="$(dirname "${BASH_SOURCE[0]}")"
defaults="${dir}/defaults.yml"
target=".github/workflows/ci.yml"

branch=$(git branch --show-current)
[[ "${branch}" == release/* ]] || { echo "Run this on a release/* branch; the current branch is ${branch}" >&2; exit 1; }
[[ -f "${target}" ]] || { echo "Run this from the repository root" >&2; exit 1; }

# The default for a key as YAML, indented to sit under "with: <key>: |"
block() { yq ".[\"$1\"]" "${defaults}" | sed 's/^/        /'; }

{
  cat "${dir}/caller-header.yml"
  echo "    uses: Alfresco/alfresco-community-repo/.github/workflows/ci.yml@master"
  echo "    secrets: inherit # pragma: allowlist secret"
  echo "    with:"
  printf '      java-version: "%s"\n' "$(yq '.["java-version"]' "${defaults}")"
  for key in db-tests activemq-profiles app-context-tests tas-tests ags-rest-api-suites; do
    printf '      %s: |\n' "${key}"
    block "${key}"
  done
} > "${target}.new"
mv "${target}.new" "${target}"
echo "Wrote ${target} for ${branch}. Review it, then commit."
