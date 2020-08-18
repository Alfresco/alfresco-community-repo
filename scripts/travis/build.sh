#!/usr/bin/env bash
set -ev
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

if [ "${TRAVIS_BUILD_STAGE_NAME,,}" = "release" ] && [ "${TRAVIS_BRANCH}" = "master" ] && isBranchBuild ; then
  PROFILES="-Pinternal"
else
  PROFILES="-PcommunityDocker"
fi

mvn -B -V install -DskipTests -Dmaven.javadoc.skip=true "${PROFILES}"


