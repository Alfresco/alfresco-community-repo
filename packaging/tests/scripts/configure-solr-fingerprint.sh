#!/usr/bin/env bash
#
# Enables the Solr fingerprint feature (alfresco.fingerprint=true) on both the
# alfresco and archive cores of a running alfresco-search-services container,
# then restarts Solr so the changes take effect.
#
# Required by tests in packaging/tests/tas-restapi/src/test/resources/
#   solr-search-e2e-fingerprint-suite.xml
#
# The default alfresco-search-services Docker image ships with this property
# set to false, which means the MinHash field is never populated and any
# FINGERPRINT:<uuid> query returns 0 results.
#
# Usage:
#   bash configure-solr-fingerprint.sh [solr-container-name]
#
# If no container name is given, the script tries to auto-detect one based on
# the alfresco-search-services image, then falls back to any container whose
# name contains "solr".
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

echo "Configuring fingerprint on Solr container: $SOLR_CONTAINER"

# Enable fingerprint on both cores
docker exec "$SOLR_CONTAINER" sh -c \
  "sed -i 's/alfresco.fingerprint=false/alfresco.fingerprint=true/g' \
   /opt/alfresco-search-services/solrhome/alfresco/conf/solrcore.properties"

docker exec "$SOLR_CONTAINER" sh -c \
  "sed -i 's/alfresco.fingerprint=false/alfresco.fingerprint=true/g' \
   /opt/alfresco-search-services/solrhome/archive/conf/solrcore.properties"

echo "--- Verification: alfresco core ---"
docker exec "$SOLR_CONTAINER" sh -c \
  "grep -i fingerprint /opt/alfresco-search-services/solrhome/alfresco/conf/solrcore.properties"

echo "--- Verification: archive core ---"
docker exec "$SOLR_CONTAINER" sh -c \
  "grep -i fingerprint /opt/alfresco-search-services/solrhome/archive/conf/solrcore.properties"

# Restart so Solr re-reads solrcore.properties
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