#!/usr/bin/env bash
echo "=========================== Starting Build ACS Event Model Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex

# ---------------------------------------------------------------------------
# Clone and install acs-event-model feature branch
# ---------------------------------------------------------------------------
ACS_EVENT_MODEL_REPO="github.com/Alfresco/acs-event-model.git"
ACS_EVENT_MODEL_BRANCH="feature/ACS-11603_applied_acl_poc"
ACS_EVENT_MODEL_DIR="$(dirname "${BASH_SOURCE[0]}")/../../../acs-event-model"

echo ">>> Cloning acs-event-model branch: ${ACS_EVENT_MODEL_BRANCH}"
rm -rf "${ACS_EVENT_MODEL_DIR}"

if [[ -n "${GIT_USERNAME}" && -n "${GIT_PASSWORD}" ]]; then
  git clone -b "${ACS_EVENT_MODEL_BRANCH}" --depth=1 \
    "https://${GIT_USERNAME}:${GIT_PASSWORD}@${ACS_EVENT_MODEL_REPO}" \
    "${ACS_EVENT_MODEL_DIR}"
else
  git clone -b "${ACS_EVENT_MODEL_BRANCH}" --depth=1 \
    "https://${ACS_EVENT_MODEL_REPO}" \
    "${ACS_EVENT_MODEL_DIR}"
fi

echo ">>> Building and installing acs-event-model"
pushd "${ACS_EVENT_MODEL_DIR}"
mvn -B -V clean install -DskipTests -Dmaven.javadoc.skip=true
popd

echo ">>> Cleaning up acs-event-model clone"
rm -rf "${ACS_EVENT_MODEL_DIR}"

set +vex
echo "=========================== Finishing Build ACS Event Model Script =========================="