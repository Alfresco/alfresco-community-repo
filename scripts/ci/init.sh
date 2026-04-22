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

# ---------------------------------------------------------------------------
# Clone and install acs-event-model
# ---------------------------------------------------------------------------
ACS_EVENT_MODEL_REPO="github.com/Alfresco/acs-event-model.git"
ACS_EVENT_MODEL_BRANCH="feature/ACS-11603_applied_acl_poc"

echo ">>> Cloning acs-event-model branch: ${ACS_EVENT_MODEL_BRANCH}"
if [[ -n "${GIT_USERNAME}" && -n "${GIT_PASSWORD}" ]]; then
  git clone -b "${ACS_EVENT_MODEL_BRANCH}" --depth=1 \
    "https://${GIT_USERNAME}:${GIT_PASSWORD}@${ACS_EVENT_MODEL_REPO}" \
    "$(dirname "${BASH_SOURCE[0]}")/../../../acs-event-model"
else
  git clone -b "${ACS_EVENT_MODEL_BRANCH}" --depth=1 \
    "https://${ACS_EVENT_MODEL_REPO}" \
    "$(dirname "${BASH_SOURCE[0]}")/../../../acs-event-model"
fi

echo ">>> Building and installing acs-event-model"
pushd "$(dirname "${BASH_SOURCE[0]}")/../../../acs-event-model"
mvn -B -V clean install -DskipTests -Dmaven.javadoc.skip=true
popd
echo ">>> acs-event-model installed successfully"
# ---------------------------------------------------------------------------

popd
set +vex
echo "=========================== Finishing Init Script =========================="

