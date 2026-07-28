#!/bin/bash
set -euo pipefail

force_prefix=""
allow_empty_commit="false"
if [[ "${COMMIT_TITLE}" =~ (\[force[^]]*\]) ]]; then
    force_prefix="${BASH_REMATCH[1]} "
    allow_empty_commit="true"
fi
message="${force_prefix}Update ${DOWNSTREAM_REPO} version to ${VERSION}"
message="${message//$'\n'/ }"
message="${message//$'\r'/ }"
printf 'message=%s\n' "${message}" >> "$GITHUB_OUTPUT"
printf 'allow-empty-commit=%s\n' "${allow_empty_commit}" >> "$GITHUB_OUTPUT"
