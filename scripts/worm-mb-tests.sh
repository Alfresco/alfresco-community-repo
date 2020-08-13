#!/usr/bin/env bash

echo "=========================== Starting Integration Tests Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"

docker login quay.io -u ${QUAY_USERNAME} -p ${QUAY_PASSWORD}

pip install awscli
printf "${CREATE_BUCKET_AWS_ACCESS_KEY}\n${CREATE_BUCKET_AWS_SECRET_KEY}\n\n\n" | aws configure

export AWS_ACCESS_KEY_ID=${CREATE_BUCKET_AWS_ACCESS_KEY}
export AWS_SECRET_ACCESS_KEY=${CREATE_BUCKET_AWS_SECRET_KEY}

export S3_BUCKET_REGION="eu-west-1"
export S3_BUCKET_NAME="travis-ags-${TRAVIS_JOB_NUMBER}"
export S3_BUCKET2_NAME="travis-ags-worm-${TRAVIS_JOB_NUMBER}-b2"
export S3_PROTOCOL=s3v2
export S3_BUCKET2_PROTOCOL=s3vTest

aws s3api create-bucket --bucket "${S3_BUCKET2_NAME}" --region us-east-1 --object-lock-enabled-for-bucket
aws s3api put-object-lock-configuration \
    --bucket "${S3_BUCKET2_NAME}" \
    --object-lock-configuration '{ "ObjectLockEnabled": "Enabled", "Rule": { "DefaultRetention": { "Mode": "COMPLIANCE", "Days": 1 }}}'

bash ./scripts/start-compose.sh rm-enterprise/rm-enterprise-share/worm-support-docker-compose.yml

# Run the WORM tests
mvn -B -U clean test \
  -DsuiteXmlFile=wormTestSuite.xml \
  -Dconnector.s3.bucketName=${S3_BUCKET2_NAME}

popd
set +vex
echo "=========================== Finishing Integration Tests Script =========================="