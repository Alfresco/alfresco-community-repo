#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

if [[ -n ${BUILD_PROFILES} ]]; then
  PROFILES="${BUILD_PROFILES}"
elif [[ "${REQUIRES_LOCAL_IMAGES}" == "true" ]]; then
  PROFILES="-Pbuild-docker-images -Pags"
else
  PROFILES="-Pags"
fi

if [[ "${REQUIRES_TAS_TESTS}" == "true" ]]; then
  PROFILES="${PROFILES} -Pall-tas-tests"
fi

if [[ "${REQUIRES_INSTALLED_ARTIFACTS}" == "true" ]]; then
  PHASE="install"
else
  PHASE="package"
fi

mvn -B -V $PHASE -DskipTests -Dmaven.javadoc.skip=true $PROFILES $BUILD_OPTIONS

# ===== TEMPORARY (cross-testing, do not merge): build ES connector from a branch =====
# When CROSSTEST_CONNECTOR_REF is set, build the connector images locally so the
# subsequent docker-compose steps use them (BATCH_INDEXING_TAG must point at the
# connector version - see packaging/tests/environment/.env).
bash "$(dirname "${BASH_SOURCE[0]}")/build-connector-crosstest.sh" || true
# ===== END TEMPORARY =====

popd
set +vex
echo "=========================== Finishing Build Script =========================="

