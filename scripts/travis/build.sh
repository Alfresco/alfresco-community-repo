#!/usr/bin/env bash
set -ex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

mvn -B -V install -DskipTests -Dmaven.javadoc.skip=true -PcommunityDocker


