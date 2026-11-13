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

# Update parent version
sed -i "/<parent>/,/<\/parent>/s|<version>.*</version>|<version>${VERSION}</version>|" pom.xml
grep -q "<version>${VERSION}</version>" <(sed -n "/<parent>/,/<\/parent>/p" pom.xml) \
  || { echo "ERROR: parent version was not updated to ${VERSION} in pom.xml"; exit 1; }

# Update dependency version
mvn -B versions:set-property versions:commit \
  -Dproperty=dependency.alfresco-community-repo.version \
  "-DnewVersion=${VERSION}"

echo "version=${VERSION}" >> "${GITHUB_OUTPUT}"

popd
set +vex
echo "=========================== Finishing Update Downstream Script =========================="
