#!/usr/bin/env bash
echo "=========================== Starting Init Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

# Maven Setup
mkdir -p "${HOME}/.m2" && cp -f .travis.settings.xml "${HOME}/.m2/settings.xml"
find "${HOME}/.m2/repository/" -type d -name "*-SNAPSHOT*" | xargs -r -l rm -rf

# Docker Logins
echo "${DOCKERHUB_PASSWORD}" | docker login -u="${DOCKERHUB_USERNAME}" --password-stdin
echo "${QUAY_PASSWORD}" | docker login -u="${QUAY_USERNAME}" --password-stdin quay.io

popd
set +vex
echo "=========================== Finishing Init Script =========================="

