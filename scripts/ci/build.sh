#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

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

if [[ -n ${BUILD_PROFILES} ]]; then
  PROFILES="${BUILD_PROFILES}"
elif [[ "${REQUIRES_LOCAL_IMAGES}" == "true" ]]; then
  PROFILES="-Pbuild-docker-images -Pags"
else
  PROFILES="-Pags"
fi

if [[ "${REQUIRES_INSTALLED_ARTIFACTS}" == "true" ]]; then
  PHASE="install"
else
  PHASE="package"
fi

mvn -B -V $PHASE -DskipTests -Dmaven.javadoc.skip=true $PROFILES $BUILD_OPTIONS

popd
set +vex
echo "=========================== Finishing Build Script =========================="

