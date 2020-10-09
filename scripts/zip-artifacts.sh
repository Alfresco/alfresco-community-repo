#!/usr/bin/env bash
set -e

if [ $1 == 'community' ]; then
    mkdir "artifacts_dir"
    cp rm-community/rm-community-repo/target/alfresco-rm-*community*amp artifacts_dir
    cp rm-community/rm-community-share/target/alfresco-rm-*community*amp artifacts_dir
    cp rm-community/rm-community-rest-api-explorer/target/alfresco-rm-*community*war artifacts_dir
    cd artifacts_dir
    zip alfresco-rm-community-${RELEASE_VERSION}.zip *amp
    ls artifacts_dir
#elif [ $1 == "enterprise" ]; then
fi
