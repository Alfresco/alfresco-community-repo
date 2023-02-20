#!/usr/bin/env bash

echo "=========================== Starting AppContext05TestSuite setup ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

mkdir -p "${HOME}/tmp"
cp repository/src/test/resources/realms/alfresco-realm.json "${HOME}/tmp"
echo "HOST_IP=$(hostname  -I | cut -f1 -d' ')" >> $GITHUB_ENV
docker run -d -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e DB_VENDOR=h2 -p 8999:8080 -e KEYCLOAK_IMPORT=/tmp/alfresco-realm.json -v $HOME/tmp/alfresco-realm.json:/tmp/alfresco-realm.json alfresco/alfresco-identity-service:1.2

popd
set +vex
echo "=========================== Finishing AppContext05TestSuite setup =========================="
