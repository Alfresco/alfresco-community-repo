#!/usr/bin/env bash
set -ev

release_type=$1

mkdir "artifacts_dir"
cp rm-${release_type}/rm-${release_type}-repo/target/alfresco-rm-${release_type}-repo-*.amp artifacts_dir
cp rm-${release_type}/rm-${release_type}-share/target/alfresco-rm-${release_type}-share-*.amp artifacts_dir
cp rm-${release_type}/rm-${release_type}-rest-api-explorer/target/alfresco-rm-${release_type}-rest-api-explorer-*.war artifacts_dir

cd artifacts_dir
zip alfresco-rm-${release_type}-${RELEASE_VERSION}.zip *

# rm *.amp *.war -f
ls artifacts_dir