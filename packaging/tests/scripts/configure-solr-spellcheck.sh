#!/usr/bin/env bash
#
# Enables the Solr spellcheck / suggester feature on a running
# alfresco-search-services container by:
#   1. Uncommenting the suggestable.property entries in shared.properties
#      (cm:name, cm:title, cm:description, cm:content).
#   2. Lowering solr.suggester.minSecsBetweenBuilds from 3600s to 30s so
#      newly-created test data appears in suggestions quickly.
# Then restarts Solr so the changes take effect.
#
# Required by tests in packaging/tests/tas-restapi/src/test/resources/
#   solr-search-e2e-spellcheck-suite.xml
#
# Usage:
#   bash configure-solr-spellcheck.sh [solr-container-name]
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

echo "Configuring spellcheck on Solr container: $SOLR_CONTAINER"

# Enable suggestable properties (cm:name, cm:title, cm:description, cm:content)
docker exec "$SOLR_CONTAINER" sh -c \
  "sed -i 's|^#alfresco.suggestable.property|alfresco.suggestable.property|g' \
   /opt/alfresco-search-services/solrhome/conf/shared.properties"

# Lower suggester rebuild interval from 1 hour to 30 seconds
docker exec "$SOLR_CONTAINER" sh -c \
  "sed -i 's|solr.suggester.minSecsBetweenBuilds=3600|solr.suggester.minSecsBetweenBuilds=30|g' \
   /opt/alfresco-search-services/solrhome/alfresco/conf/solrcore.properties"

docker exec "$SOLR_CONTAINER" sh -c \
  "sed -i 's|solr.suggester.minSecsBetweenBuilds=3600|solr.suggester.minSecsBetweenBuilds=30|g' \
   /opt/alfresco-search-services/solrhome/archive/conf/solrcore.properties"

echo "--- Verification: shared.properties ---"
docker exec "$SOLR_CONTAINER" sh -c \
  "grep -i suggestable /opt/alfresco-search-services/solrhome/conf/shared.properties"

echo "--- Verification: alfresco core ---"
docker exec "$SOLR_CONTAINER" sh -c \
  "grep -i minSecs /opt/alfresco-search-services/solrhome/alfresco/conf/solrcore.properties"

echo "--- Verification: archive core ---"
docker exec "$SOLR_CONTAINER" sh -c \
  "grep -i minSecs /opt/alfresco-search-services/solrhome/archive/conf/solrcore.properties"

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