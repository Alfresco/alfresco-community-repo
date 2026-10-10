#!/usr/bin/env bash
# =============================================================================
# TEMPORARY - cross-testing helper (NOT for merge).
#
# Builds the Alfresco Elasticsearch Connector Docker images locally from a
# feature branch instead of pulling a published tag from docker.io/quay.io.
#
# Activated only when CROSSTEST_CONNECTOR_REF is set (e.g. in the workflow env),
# so normal CI runs are completely unaffected.
#
# buildDockerImagesCi.sh tags the batch-indexing image as
# alfresco/alfresco-elasticsearch-batch-indexing:<connector-version> in the local
# Docker daemon. Callers must set BATCH_INDEXING_TAG to that version so docker
# compose uses the locally-built image (compose never pulls when the image with
# that exact name:tag already exists locally).
# =============================================================================
set -euo pipefail

CONNECTOR_REF="${CROSSTEST_CONNECTOR_REF:-}"
if [ -z "${CONNECTOR_REF}" ]; then
  exit 0
fi

CONNECTOR_REPO="${CROSSTEST_CONNECTOR_REPO:-github.com/Alfresco/alfresco-elasticsearch-connector.git}"
CONNECTOR_DIR="${CROSSTEST_CONNECTOR_DIR:-${RUNNER_TEMP:-/tmp}/alfresco-elasticsearch-connector-crosstest}"

if [ -n "${CROSSTEST_CONNECTOR_TOKEN:-}" ]; then
  CLONE_URL="https://x-access-token:${CROSSTEST_CONNECTOR_TOKEN}@${CONNECTOR_REPO}"
elif [ -n "${GIT_USERNAME:-}" ] && [ -n "${GIT_PASSWORD:-}" ]; then
  CLONE_URL="https://${GIT_USERNAME}:${GIT_PASSWORD}@${CONNECTOR_REPO}"
else
  CLONE_URL="https://${AUTH:-}${CONNECTOR_REPO}"
fi

echo "[crosstest] Building ES connector images from ${CONNECTOR_REPO}@${CONNECTOR_REF}" 1>&2

rm -rf "${CONNECTOR_DIR}"
git clone -b "${CONNECTOR_REF}" --depth=1 "${CLONE_URL}" "${CONNECTOR_DIR}" 1>&2

pushd "${CONNECTOR_DIR}" >/dev/null
  CONNECTOR_VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"
  mvn -B -ntp -V -q clean install -DskipTests -Dmaven.javadoc.skip=true -Pdocker-image 1>&2
  bash scripts/ci/buildDockerImagesCi.sh 1>&2
popd >/dev/null

echo "[crosstest] Built connector images tagged :${CONNECTOR_VERSION}" 1>&2
echo "${CONNECTOR_VERSION}"
