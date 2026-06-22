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
# If no container name is given, the script auto-detects the Solr container.
# Detection order:
#   1. The container labelled with Compose service name "solr6"
#      (com.docker.compose.service=solr6). This is the canonical case for any
#      docker-compose file in this repo, independent of SOLR6_TAG / image tag.
#   2. Any container whose image name starts with "alfresco/alfresco-search-services"
#      or "quay.io/alfresco/insight-engine" (tag-agnostic).
#   3. Any container whose name contains "solr".
#
set -euo pipefail

SOLR_CONTAINER="${1:-}"

# 1. Compose service label (independent of image tag / SOLR6_TAG)
if [ -z "$SOLR_CONTAINER" ]; then
  SOLR_CONTAINER=$(docker ps \
    --filter "label=com.docker.compose.service=solr6" \
    --format "{{.Names}}" | head -n1)
fi

# 2. Image repository (no tag), covers community + enterprise/Insight Engine
if [ -z "$SOLR_CONTAINER" ]; then
  SOLR_CONTAINER=$(docker ps --format "{{.Names}} {{.Image}}" \
    | awk '$2 ~ /^(docker\.io\/)?alfresco\/alfresco-search-services(:|$)/ \
           || $2 ~ /^quay\.io\/alfresco\/insight-engine(:|$)/ { print $1; exit }')
fi

# 3. Last-ditch fallback: name contains "solr"
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

# Poll Solr from INSIDE the container on its native port (8983).
# This avoids any host-side networking / port-mapping flakiness
# (host port 8083 -> container port 8983 in docker-compose).
# We use wget (always present in the alfresco-search-services image)
# instead of curl (not guaranteed on every CI runner / Git Bash setup).
echo "Waiting for Solr to come back online..."
for i in $(seq 1 60); do
  if docker exec "$SOLR_CONTAINER" sh -c \
       "wget -qO- 'http://localhost:8983/solr/admin/cores?action=STATUS' >/dev/null 2>&1"; then
    echo "Solr is back up after $i attempt(s)."
    exit 0
  fi
  sleep 2
done

# Don't fail the build here: the sed patch and restart already succeeded,
# and Maven's own waitForIndexing() will retry on slow Solr startup.
echo "WARN: Solr did not respond to STATUS within 120s, but patch was applied. Continuing." >&2
exit 0