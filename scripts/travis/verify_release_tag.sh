#!/usr/bin/env bash
echo "=========================== Starting Verify Release Tag Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

#
# Check that the version to be released does not already have a git tag.
#

POM_VERSION=$(mvn -B -q help:evaluate -Dexpression=project.version -DforceStdout)
printf "POM version: %s\n" "${POM_VERSION}"

TAG="${POM_VERSION%-SNAPSHOT}"

if git rev-parse "${TAG}^{tag}" &>/dev/null ; then
  echo "The next tag \"${TAG}\" already exists in the git project"
  exit 1
fi

popd
set +vex
echo "=========================== Finishing Verify Release Tag Script =========================="

