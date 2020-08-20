#!/usr/bin/env bash

echo "=========================== Starting Worm Tests Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"

export S3_BUCKET2_NAME="travis-ags-worm-${TRAVIS_JOB_NUMBER}-b2"

cd rm-automation/rm-automation-enterprise-rest-api

# Run the WORM tests
mvn -B -U clean test \
  -DsuiteXmlFile=wormTestSuite.xml \
  -Dskip.automationtests=false \
  -Dconnector.s3.bucketName=${S3_BUCKET2_NAME}

popd
set +vex
echo "=========================== Finishing Worm Tests Script =========================="
