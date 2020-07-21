#!/usr/bin/env bash
set -ev

if [ -z ${COMM_RELEASE_VERSION} ] || [ -z ${RELEASE_VERSION} ];
then
  echo "Please provide a COMM_RELEASE_VERSION and RELEASE_VERSION in the format <acs-version>-<additional-info> (6.3.0-EA or 6.3.0-SNAPSHOT)"
  exit -1
fi

build_number=$1
branch_name=$2
build_stage=release
SOURCE=s3://alfresco-artefacts-staging/alfresco-content-services-community/$build_stage/$branch_name/$build_number
DESTINATION=s3://eu.dl.alfresco.com/release/community/$COMM_RELEASE_VERSION-build-$build_number

aws s3 cp --acl private $SOURCE/alfresco.war $DESTINATION/alfresco.war
aws s3 cp --acl private $SOURCE/alfresco-content-services-community-distribution-$RELEASE_VERSION.zip $DESTINATION/alfresco-content-services-community-distribution-$RELEASE_VERSION.zip