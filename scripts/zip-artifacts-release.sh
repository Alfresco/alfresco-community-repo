#!/usr/bin/env bash
set -evx

release_type=$1

mkdir "artifacts_dir"
cp rm-${release_type}/rm-${release_type}-repo/target/alfresco-rm-*${release_type}*amp artifacts_dir
cp rm-${release_type}/rm-${release_type}-share/target/alfresco-rm-*${release_type}*amp artifacts_dir
cp rm-${release_type}/rm-${release_type}-rest-api-explorer/target/alfresco-rm-*${release_type}*war artifacts_dir

mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:copy \
    -Dartifact=org.alfresco:alfresco-rm-${release_type}-repo:${RELEASE_VERSION}:amp \
    -DoutputDirectory=artifacts_dir

mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:copy \
    -Dartifact=org.alfresco:alfresco-rm-${release_type}-share:${RELEASE_VERSION}:amp \
    -DoutputDirectory=artifacts_dir

mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:copy \
    -Dartifact=org.alfresco:alfresco-rm-${release_type}-rest-api-explorer:${RELEASE_VERSION}:war \
    -DoutputDirectory=artifacts_dir

cd artifacts_dir
zip alfresco-rm-${release_type}-${RELEASE_VERSION}.zip *

# rm *.amp *.war -f
ls artifacts_dir