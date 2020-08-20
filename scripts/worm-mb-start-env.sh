#!/usr/bin/env bash

echo "=========================== Starting MB Env Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"

docker login quay.io -u ${QUAY_USERNAME} -p ${QUAY_PASSWORD}

export AWS_ACCESS_KEY_ID=${CREATE_BUCKET_AWS_ACCESS_KEY}
export AWS_SECRET_ACCESS_KEY=${CREATE_BUCKET_AWS_SECRET_KEY}

export S3_BUCKET_REGION="us-east-1"
export S3_BUCKET_NAME="travis-ags-${TRAVIS_JOB_NUMBER}"
export S3_BUCKET2_NAME="travis-ags-worm-${TRAVIS_JOB_NUMBER}-b2"
export S3_PROTOCOL=s3v2
export S3_BUCKET2_PROTOCOL=s3vTest

bash scripts/start-compose.sh "${PWD}/rm-enterprise/rm-enterprise-share/worm-support-docker-compose.yml"

popd
set +vex
echo "=========================== Finishing MB Env Script =========================="
