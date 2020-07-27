#!/usr/bin/env bash
set -ev
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

mvn -B -V install -DskipTests -Dversion.edition=Community -PcommunityDocker


