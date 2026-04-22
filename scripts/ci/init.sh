#!/usr/bin/env bash
echo "=========================== Starting Init Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

# Maven Setup
find "${HOME}/.m2/repository/" -type d -name "*-SNAPSHOT*" | xargs -r -l rm -rf

# Docker Logins
echo "${DOCKERHUB_PASSWORD}" | docker login -u="${DOCKERHUB_USERNAME}" --password-stdin
echo "${QUAY_PASSWORD}" | docker login -u="${QUAY_USERNAME}" --password-stdin quay.io

popd
set +vex
echo "=========================== Finishing Init Script =========================="

# Build and install acs-event-model so the 1.1.1-A.1-SNAPSHOT jar is
# available before any Maven build in this job runs.
bash "$(dirname "${BASH_SOURCE[0]}")/build_acs_event_model.sh"