#!/usr/bin/env bash
set -ev
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

PROFILES="$([ "${TRAVIS_BUILD_STAGE_NAME,,}" = "release" ] && echo "-Pinternal" || echo "-PcommunityDocker" )"

mvn -B -V install -DskipTests -Dmaven.javadoc.skip=true "${PROFILES}"


