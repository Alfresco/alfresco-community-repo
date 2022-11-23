#!/usr/bin/env bash
set +vx

function cloneRepo() {
  local REPO="${1}"
  local TAG_OR_BRANCH="${2}"

  printf "Cloning \"%s\" on %s\n" "${TAG_OR_BRANCH}" "${REPO}"

  # clone the repository branch/tag
  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../" >/dev/null

  rm -rf "$(basename "${REPO%.git}")"

  git clone -b "${TAG_OR_BRANCH}" --depth=1 "https://${GIT_USERNAME}:${GIT_PASSWORD}@${REPO}"

  popd >/dev/null
}

set -vx
