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
mvn -B versions:update-parent versions:commit "-DparentVersion=[${VERSION}]"

# Update dependency version
mvn -B versions:set-property versions:commit \
  -Dproperty=dependency.alfresco-community-repo.version \
  "-DnewVersion=${VERSION}"

# Commit changes
git status
git --no-pager diff pom.xml
git add pom.xml

if [[ "${COMMIT_MESSAGE}" =~ \[force[^\]]*\] ]]; then
  FORCE_TOKEN=$(echo "${COMMIT_MESSAGE}" | sed "s|^.*\(\[force[^]]*\]\).*$|\1|g")
  git commit --allow-empty -m "${FORCE_TOKEN} Update upstream community-repo version to ${VERSION}"
  git push
elif git status --untracked-files=no --porcelain | grep -q '^' ; then
  git commit -m "Update upstream community-repo version to ${VERSION}"
  git push
else
  echo "Dependencies are already up to date."
  git status
fi


popd
set +vex
echo "=========================== Finishing Update Downstream Script =========================="

