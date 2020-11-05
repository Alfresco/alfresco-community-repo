#!/usr/bin/env bash
set -e

release_type=$1

mkdir "artifacts_dir"

mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:copy \
    -Dartifact=org.alfresco:alfresco-governance-services-${release_type}-repo:${RELEASE_VERSION}:amp \
    -DoutputDirectory=artifacts_dir

mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:copy \
    -Dartifact=org.alfresco:alfresco-governance-services-${release_type}-share:${RELEASE_VERSION}:amp \
    -DoutputDirectory=artifacts_dir

mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:copy \
    -Dartifact=org.alfresco:alfresco-governance-services-${release_type}-rest-api-explorer:${RELEASE_VERSION}:war \
    -DoutputDirectory=artifacts_dir

cd artifacts_dir
zip alfresco-governance-services-${release_type}-${RELEASE_VERSION}.zip *

# rm *.amp *.war -f
ls