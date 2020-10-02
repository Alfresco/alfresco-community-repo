#!/bin/bash
set -e

if [ $1 == 'community' ]; then
    mkdir "artifacts_dir"
    cp rm-community/rm-community-repo/target/alfresco-rm-*community*amp artifacts_dir
    cp rm-community/rm-community-share/target/alfresco-rm-*community*amp artifacts_dir
    zip alfresco-rm-community-${RELEASE_VERSION}.zip *amp
    cp alfresco-rm-community-${RELEASE_VERSION}.zip artifacts_dir
    ls artifacts_dir
#elif [ $1 == "enterprise" ]; then
fi
