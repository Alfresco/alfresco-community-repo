#!/usr/bin/env bash

set -e

M2_REPO_DIR="$HOME/.m2/repository"
M2_REPO_FILE_COUNT="$(find $M2_REPO_DIR -type f 2>/dev/null | wc -l)"

echo "Files in the maven repo: $M2_REPO_FILE_COUNT"

echo "Verifying compilation and ensuring maven cache populated."
export BUILD_PROFILES="-Pall-tas-tests,ags"
export BUILD_OPTIONS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.artifact.threads=8"
source "$(dirname "${BASH_SOURCE[0]}")/build.sh"
