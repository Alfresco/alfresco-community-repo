#!/usr/bin/env bash
#
# Enables the Solr cross-locale tokenisation feature on a running
# alfresco-search-services container by:
#   1. Uncommenting the alfresco.cross.locale.datatype.N entries in
#      shared.properties (text, content, mltext).
# Then restarts Solr so the new analysers are registered.
#
# Required by tests in packaging/tests/tas-restapi/src/test/resources/
#   solr-search-e2e-crosslocale-suite.xml
#
# Usage:
#   bash configure-solr-crosslocale.sh [solr-container-name]
#
set -euo pipefail

SOLR_CONTAINER="${1:-}"

if [ -z "$SOLR_CONTAINER" ]; then
  SOLR_CONTAINER=$(docker ps \
    --filter "ancestor=docker.io/alfresco/alfresco-search-services:2.0.20" \
    --format "{{.Names}}" | head -n1)
fi

if [ -z "$SOLR_CONTAINER" ]; then
  SOLR_CONTAINER=$(docker ps --format "{{.Names}}" | grep -i solr | head -n1 || true)
fi

if [ -z "$SOLR_CONTAINER" ]; then
  echo "ERROR: Could not locate a running Solr container." >&2
  docker ps >&2
  exit 1
fi

echo "Configuring cross-locale on Solr container: $SOLR_CONTAINER"

# Uncomment alfresco.cross.locale.datatype.N=... in shared.properties
docker exec "$SOLR_CONTAINER" sh -c \
  "sed -i 's|^#alfresco.cross.locale.datatype|alfresco.cross.locale.datatype|g' \
   /opt/alfresco-search-services/solrhome/conf/shared.properties"

echo "--- Verification: shared.properties ---"
docker exec "$SOLR_CONTAINER" sh -c \
  "grep -n -E 'cross\.locale' /opt/alfresco-search-services/solrhome/conf/shared.properties"

# Restart so Solr re-reads the changed properties
docker restart "$SOLR_CONTAINER"

echo "Waiting for Solr to come back online..."
for i in $(seq 1 60); do
  if curl -sf "http://localhost:8083/solr/admin/cores?action=STATUS" > /dev/null 2>&1; then
    echo "Solr is back up after $i attempt(s)."
    exit 0
  fi
  sleep 2
done

echo "ERROR: Solr did not come back online within 120s." >&2
exit 1