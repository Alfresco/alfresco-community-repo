#!/usr/bin/env bash

echo "=========================== Starting Integration Tests Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"

export AWS_ACCESS_KEY_ID=${CREATE_BUCKET_AWS_ACCESS_KEY}
export AWS_SECRET_ACCESS_KEY=${CREATE_BUCKET_AWS_SECRET_KEY}

export BUCKET_NAME="travis-ags-worm-${TRAVIS_BUILD_NUMBER}-${TRAVIS_JOB_NUMBER}"
export BUCKET2_NAME="travis-ags-worm-${TRAVIS_BUILD_NUMBER}-${TRAVIS_JOB_NUMBER}-b2"

export S3_BUCKET_REGION="eu-west-1"
export S3_BUCKET_NAME="${BUCKET_NAME}"
export S3_BUCKET2_NAME="${BUCKET2_NAME}"
export S3_PROTOCOL=s3v2
export S3_BUCKET2_PROTOCOL=s3vTest

mvn -B -U clean install -DskipTests -Pbuild-test-image 

./scripts/start-compose.sh ./rm-enterprise/rm-enterprise-share/worm-support-docker-compose.yml

# Run the WORM tests
# mvn -B -U clean test \
#   -Prun-tas-tests,run-multiple-buckets-tests \
#   -Denvironment=default \
#   -DrunBugs=false \
#   -Dalfresco.port=8080 \
#   -Dconnector.s3.bucketName=${BUCKET_NAME2}


popd
set +vex
echo "=========================== Finishing Integration Tests Script =========================="