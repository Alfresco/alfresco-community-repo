#!/usr/bin/env bash
echo "=========================== Starting Update Downstream Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

DOWNSTREAM_REPO="github.com/Alfresco/alfresco-enterprise-repo.git"

cloneRepo "${DOWNSTREAM_REPO}" "${TRAVIS_BRANCH}"

cd "$(dirname "${BASH_SOURCE[0]}")/../../../$(basename "${DOWNSTREAM_REPO%.git}")"

# Update parent
mvn versions:update-parent versions:commit

VERSION="$(sed -n '/<parent>/,/<\/parent>/p' pom.xml \
    | sed -n '/<version>/,/<\/version>/p' \
    | tr -d '\n' \
    | grep -oP '(?<=<version>).*(?=</version>)' \
    | xargs)"

# Update dependency version
mvn versions:set-property versions:commit \
  -Dproperty=dependency.alfresco-community-repo.version \
  "-DnewVersion=${VERSION}"

# Commit changes
git status
git --no-pager diff pom.xml
git add pom.xml

if git status --untracked-files=no --porcelain | grep -q '^' ; then
  git commit -m "Update upstream version to ${VERSION}"
  git push
else
  echo "Dependencies are already up to date."
  git status
fi


popd
set +vex
echo "=========================== Finishing Update Downstream Script =========================="

