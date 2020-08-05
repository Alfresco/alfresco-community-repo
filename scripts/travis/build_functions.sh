#!/usr/bin/env bash

function isPullRequestBuild() {
  test "${TRAVIS_PULL_REQUEST}" != "false"
}

function isBranchBuild() {
  test "${TRAVIS_PULL_REQUEST}" = "false"
}

function cloneRepo() {
  local REPO="${1}"
  local TAG_OR_BRANCH="${2}"

  # clone the repository branch/tag
  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../" >/dev/null

  rm -rf "$(basename "${REPO%.git}")"

  git clone -b "${TAG_OR_BRANCH}" --depth=1 "https://${GIT_USERNAME}:${GIT_PASSWORD}@${REPO}"

  popd >/dev/null
}

function retrievePomParentVersion() {
  pushd "$(dirname "${BASH_SOURCE[0]}")/../../" >/dev/null

  sed -n '/<parent>/,/<\/parent>/p' pom.xml \
    | sed -n '/<version>/,/<\/version>/p' \
    | tr -d '\n' \
    | grep -oP '(?<=<version>).*(?=</version>)' \
    | xargs

  popd >/dev/null
}

function retrievePomProperty() {
  local KEY="${1}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../" >/dev/null

  sed -n '/<properties>/,/<\/properties>/p' pom.xml \
    | sed -n "/<${KEY}>/,/<\/${KEY}>/p" \
    | tr -d '\n' \
    | grep -oP "(?<=<${KEY}>).*(?=</${KEY}>)" \
    | xargs

  popd >/dev/null
}

function evaluatePomProperty() {
  local KEY="${1}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../" >/dev/null

  mvn -B -q help:evaluate -Dexpression="${KEY}" -DforceStdout

  popd >/dev/null
}

function remoteBranchExists() {
  local REMOTE_REPO="${1}"
  local BRANCH="${2}"

  git ls-remote --exit-code --heads "https://${GIT_USERNAME}:${GIT_PASSWORD}@${REMOTE_REPO}" "${BRANCH}"
}

function pullUpstreamTag() {
  local UPSTREAM_REPO="${1}"
  local TAG="${2}"

  cloneRepo "${UPSTREAM_REPO}" "${TAG}"
}

function pullAndBuildSameBranchOnUpstream() {
  local UPSTREAM_REPO="${1}"
  local EXTRA_BUILD_ARGUMENTS="${2}"
  local SOURCE_BRANCH="$(isBranchBuild && echo "${TRAVIS_BRANCH}" || echo "${TRAVIS_PULL_REQUEST_BRANCH}")"

  if ! remoteBranchExists "${UPSTREAM_REPO}" "${SOURCE_BRANCH}" ; then
    printf "Branch \"%s\" not found on the %s repository\n" "${SOURCE_BRANCH}" "${UPSTREAM_REPO}"
    exit 1
  fi

  cloneRepo "${UPSTREAM_REPO}" "${SOURCE_BRANCH}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

  cd "$(basename "${UPSTREAM_REPO%.git}")"

  mvn -B -V -q clean install -DskipTests -Dmaven.javadoc.skip=true ${EXTRA_BUILD_ARGUMENTS}
  mvn -B -V install -DskipTests -f packaging/tests/pom.xml

  popd
}

