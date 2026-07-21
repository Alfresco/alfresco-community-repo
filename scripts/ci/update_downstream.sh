#!/usr/bin/env bash
echo "=========================== Starting Update Downstream Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

#Fetch the latest changes, as GHA will only checkout the PR commit
git fetch origin "feature/ACS-12079_use_github_app_to_release_test"
git checkout "feature/ACS-12079_use_github_app_to_release_test"
git pull

# Retrieve the current Community version - latest tag on the current branch
VERSION="$(git describe --abbrev=0 --tags)"

DOWNSTREAM_REPO="github.com/Alfresco/alfresco-enterprise-repo.git"

cloneRepo "${DOWNSTREAM_REPO}" "feature/ACS-12079_use_github_app_to_release_test"

cd "$(dirname "${BASH_SOURCE[0]}")/../../../$(basename "${DOWNSTREAM_REPO%.git}")"

# Update parent version
mvn -B versions:update-parent versions:commit "-DparentVersion=[${VERSION}]"

# Update dependency version
mvn -B versions:set-property versions:commit \
  -Dproperty=dependency.alfresco-community-repo.version \
  "-DnewVersion=${VERSION}"

popd
set +vex
echo "=========================== Finishing Update Downstream Script =========================="
