#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"


# Build the current project
mvn -B -V install -DskipTests -Dmaven.javadoc.skip=true -Pbuild-docker-images -Pags


popd
set +vex
echo "=========================== Finishing Build Script =========================="

