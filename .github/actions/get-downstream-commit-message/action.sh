#!/bin/bash
set -euo pipefail

force_prefix=""
allow_empty_commit="false"
if [[ "${COMMIT_TITLE}" =~ (\[force[^]]*\]) ]]; then
    force_prefix="${BASH_REMATCH[1]}"
    if [[ "${TRIGGER_RELEASE_ON_FORCE:-false}" == "true" ]]; then
        force_prefix="${force_prefix}[release][skip tests]"
    fi
    force_prefix="${force_prefix}"
    allow_empty_commit="true"
fi

directives_prefix=""
if [[ -n "${DIRECTIVES:-}" ]]; then
    directives="${DIRECTIVES}"
    if [[ "${COMMIT_TITLE}" =~ \[publish\] ]] && [[ "${directives}" != *"[publish]"* ]]; then
        directives="${directives}[publish]"
    fi
    directives_prefix="${directives}"
fi

# Build the message: tokens are concatenated without spaces; a single trailing
# space separates the token block from the message body.
if [[ -n "${force_prefix}${directives_prefix}" ]]; then
    token_block="${force_prefix}${directives_prefix} "
else
    token_block=""
fi

if [[ -z "${DOWNSTREAM_REPO:-}" ]]; then
    message="${token_block}${VERSION}"
else
    message="${token_block}Update ${DOWNSTREAM_REPO} version to ${VERSION}"
fi
message="${message//$'\n'/ }"
message="${message//$'\r'/ }"

if [[ -n "${PENDING_DOWNSTREAM:-}" ]]; then
    if [[ "${BRANCH_NAME}" == "master" ]]; then
        directive="[skip docker_latest]"
    else
        directive="[skip docker_release]"
    fi
    message="${message}

${directive} until ${PENDING_DOWNSTREAM} triggers the build or it is built manually"
fi

printf 'allow-empty-commit=%s\n' "${allow_empty_commit}" >> "$GITHUB_OUTPUT"
{
    printf 'message<<EOF\n'
    printf '%s\n' "${message}"
    printf 'EOF\n'
} >> "$GITHUB_OUTPUT"
