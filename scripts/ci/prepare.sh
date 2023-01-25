#!/usr/bin/env bash

set -e

M2_REPO_DIR="$HOME/.m2/repository"
M2_REPO_TTL_MINUTES=10080
M2_REPO_EXPIRED="$(find $M2_REPO_DIR -type f -mmin +$M2_REPO_TTL_MINUTES 2>/dev/null | head -n 1 | wc -l)"
M2_REPO_FILE_COUNT="$(find $M2_REPO_DIR -type f 2>/dev/null | wc -l)"

ORG_ALFRESCO_M2_REPO_DIR="$M2_REPO_DIR/org/alfresco"
ORG_ALFRESCO_M2_REPO_TTL_MINUTES=1440
ORG_ALFRESCO_M2_REPO_EXPIRED="$(find $ORG_ALFRESCO_M2_REPO_DIR -type f -mmin +$ORG_ALFRESCO_M2_REPO_TTL_MINUTES 2>/dev/null | head -n 1 | wc -l)"

echo "Files in the maven repo: $M2_REPO_FILE_COUNT"

if [ $ORG_ALFRESCO_M2_REPO_EXPIRED -eq 1 ];then
  echo "Invalidating org/alfresco maven local cache."
  rm -rf "$ORG_ALFRESCO_M2_REPO_DIR"
fi

if [ $M2_REPO_EXPIRED -eq 1 ];then
  echo "Invalidating maven local cache."
  rm -rf "$M2_REPO_DIR"
fi

echo "Verifying compilation and ensuring maven cache populated."
export BUILD_PROFILES="-Pall-tas-tests,ags"
export BUILD_OPTIONS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.artifact.threads=8"
source "$(dirname "${BASH_SOURCE[0]}")/build.sh"
