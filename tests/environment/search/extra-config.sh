#!/usr/bin/env bash
set -e

# Runs trackers once a second by modifying the template config of cores
sed -i 's|alfresco.cron=0/10 \* \* \* \* ? \*|alfresco.cron=\* \* \* ? \* \*|' \
    /opt/alfresco-search-services/solrhome/templates/rerank/conf/solrcore.properties
sed -i 's/alfresco.commitInterval=2000/alfresco.commitInterval=200/' \
    /opt/alfresco-search-services/solrhome/templates/rerank/conf/solrcore.properties
sed -i 's/alfresco.newSearcherInterval=3000/alfresco.newSearcherInterval=300/' \
    /opt/alfresco-search-services/solrhome/templates/rerank/conf/solrcore.properties