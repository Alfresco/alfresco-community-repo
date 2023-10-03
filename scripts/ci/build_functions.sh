#!/usr/bin/env bash
set +vx

function isPullRequestBuild() {
  test "${PULL_REQUEST}" != "false"
}

function isBranchBuild() {
  test "${PULL_REQUEST}" = "false"
}

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

function retrievePomParentVersion() {
  local REPO="${1}"

  if [ -z "${REPO}" ]; then
    pushd "$(dirname "${BASH_SOURCE[0]}")/../../" >/dev/null
  else
    pushd "$(dirname "${BASH_SOURCE[0]}")/../../../$(basename "${REPO%.git}")" >/dev/null
  fi

  sed -n '/<parent>/,/<\/parent>/p' pom.xml \
    | sed -n '/<version>/,/<\/version>/p' \
    | tr -d '\n' \
    | grep -oP '(?<=<version>).*(?=</version>)' \
    | xargs

  popd >/dev/null
}

function retrievePomProperty() {
  local KEY="${1}"
  local REPO="${2}"

  if [ -z "${REPO}" ]; then
    pushd "$(dirname "${BASH_SOURCE[0]}")/../../" >/dev/null
  else
    pushd "$(dirname "${BASH_SOURCE[0]}")/../../../$(basename "${REPO%.git}")" >/dev/null
  fi

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

  git ls-remote --exit-code --heads "https://${GIT_USERNAME}:${GIT_PASSWORD}@${REMOTE_REPO}" "${BRANCH}" &>/dev/null
}

function identifyUpstreamSourceBranch() {
  local UPSTREAM_REPO="${1}"

  # otherwise use the current branch name (or in case of PRs, the target branch name)
  if remoteBranchExists "${UPSTREAM_REPO}" "${BRANCH_NAME}" ; then
    echo "${BRANCH_NAME}"
    exit 0
  fi

  # if none of the previous exists, use the "master" branch
  echo "master"
}

function pullUpstreamTag() {
  local UPSTREAM_REPO="${1}"
  local TAG="${2}"

  cloneRepo "${UPSTREAM_REPO}" "${TAG}"
}

function pullSameBranch() {
  local UPSTREAM_REPO="${1}"

  local SOURCE_BRANCH="$(identifyUpstreamSourceBranch "${UPSTREAM_REPO}")"

  cloneRepo "${UPSTREAM_REPO}" "${SOURCE_BRANCH}"
}

function buildUpstreamTag() {
  local UPSTREAM_REPO="${1}"
  local TAG="${2}"
  local EXTRA_BUILD_ARGUMENTS="${3}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

  cd "$(basename "${UPSTREAM_REPO%.git}")"

  mvn -B -V clean package -DskipTests -Dmaven.javadoc.skip=true "-Dimage.tag=${TAG}" ${EXTRA_BUILD_ARGUMENTS}

  popd
}

function buildSameBranchOnUpstream() {
  local UPSTREAM_REPO="${1}"
  local EXTRA_BUILD_ARGUMENTS="${2}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

  cd "$(basename "${UPSTREAM_REPO%.git}")"

  mvn -B -V -q clean install -DskipTests -Dmaven.javadoc.skip=true ${EXTRA_BUILD_ARGUMENTS}
  mvn -B -V -q install -DskipTests -f packaging/tests/pom.xml

  popd
}

function pullUpstreamTagAndBuildDockerImage() {
  local UPSTREAM_REPO="${1}"
  local TAG="${2}"
  local EXTRA_BUILD_ARGUMENTS="${3}"

  cloneRepo "${UPSTREAM_REPO}" "${TAG}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

  cd "$(basename "${UPSTREAM_REPO%.git}")"

  mvn -B -V clean package -DskipTests -Dmaven.javadoc.skip=true "-Dimage.tag=${TAG}" ${EXTRA_BUILD_ARGUMENTS}

  popd
}

function pullAndBuildSameBranchOnUpstream() {
  local UPSTREAM_REPO="${1}"
  local EXTRA_BUILD_ARGUMENTS="${2}"

  local SOURCE_BRANCH="$(identifyUpstreamSourceBranch "${UPSTREAM_REPO}")"

  cloneRepo "${UPSTREAM_REPO}" "${SOURCE_BRANCH}"

  pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

  cd "$(basename "${UPSTREAM_REPO%.git}")"

  mvn -B -V -q clean install -DskipTests -Dmaven.javadoc.skip=true ${EXTRA_BUILD_ARGUMENTS}
  mvn -B -V -q install -DskipTests -f packaging/tests/pom.xml

  popd
}

function retieveLatestTag() {
  local REPO="${1}"
  local BRANCH="${2}"

  local LOCAL_PATH="/tmp/$(basename "${REPO%.git}")"

  git clone -q -b "${BRANCH}" "https://${GIT_USERNAME}:${GIT_PASSWORD}@${REPO}" "${LOCAL_PATH}"

  pushd "${LOCAL_PATH}" >/dev/null
  git describe --abbrev=0 --tags
  popd >/dev/null

  rm -rf "${LOCAL_PATH}"
}

set -vx