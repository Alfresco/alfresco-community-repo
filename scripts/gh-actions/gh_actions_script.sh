#!/usr/bin/env bash
echo "=========================== Starting Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pwd
echo "${BUILD_NUMBER}"
echo "${GIT_USERNAME}"
echo "${GIT_PASSWORD}"
POM_VERSION=$(mvn -B -q help:evaluate -Dexpression=project.version -DforceStdout)
printf "POM version: %s\n" "${POM_VERSION}"
TAG="${POM_VERSION%-SNAPSHOT}"
echo "${TAG}"

set +vex
echo "=========================== Finishing Script =========================="