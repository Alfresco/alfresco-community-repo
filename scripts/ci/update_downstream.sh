#!/usr/bin/env bash
echo "=========================== Starting Update Downstream Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

#Fetch the latest changes, as GHA will only checkout the PR commit
git fetch origin "${BRANCH_NAME}"
git checkout "${BRANCH_NAME}"
git pull

# Retrieve the current Community version - latest tag on the current branch
VERSION="$(git describe --abbrev=0 --tags)"

DOWNSTREAM_REPO="github.com/Alfresco/alfresco-enterprise-repo.git"

cloneRepo "${DOWNSTREAM_REPO}" "${BRANCH_NAME}"

cd "$(dirname "${BASH_SOURCE[0]}")/../../../$(basename "${DOWNSTREAM_REPO%.git}")"

# Pin the versions plugin: an unpinned "versions:" prefix resolves to the latest release (2.24.0 since the
# Cloudsmith migration), which silently skips update-parent. 2.7 is the last version known to work.
VERSIONS_PLUGIN="org.codehaus.mojo:versions-maven-plugin:2.7"

# Update parent version
mvn -B "${VERSIONS_PLUGIN}:update-parent" "${VERSIONS_PLUGIN}:commit" "-DparentVersion=[${VERSION}]"

# Update dependency version
mvn -B "${VERSIONS_PLUGIN}:set-property" "${VERSIONS_PLUGIN}:commit" \
  -Dproperty=dependency.alfresco-community-repo.version \
  "-DnewVersion=${VERSION}"

echo "version=${VERSION}" >> "${GITHUB_OUTPUT}"

popd
set +vex
echo "=========================== Finishing Update Downstream Script =========================="
