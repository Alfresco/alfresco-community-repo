#!/usr/bin/env bash

echo "=========================== Create Worm Bucket ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"

pip install awscli
printf "${CREATE_BUCKET_AWS_ACCESS_KEY}\n${CREATE_BUCKET_AWS_SECRET_KEY}\n\n\n" | aws configure

export AWS_ACCESS_KEY_ID=${CREATE_BUCKET_AWS_ACCESS_KEY}
export AWS_SECRET_ACCESS_KEY=${CREATE_BUCKET_AWS_SECRET_KEY}

export S3_BUCKET_REGION="eu-west-1"
export S3_BUCKET2_NAME="travis-ags-worm-${TRAVIS_JOB_NUMBER}-b2"
export S3_PROTOCOL=s3v2
export S3_BUCKET2_PROTOCOL=s3vTest

aws s3api create-bucket --bucket "${S3_BUCKET2_NAME}" --region us-east-1 --object-lock-enabled-for-bucket
aws s3api put-object-lock-configuration \
    --bucket "${S3_BUCKET2_NAME}" \
    --object-lock-configuration 'ObjectLockEnabled=Enabled,Rule={DefaultRetention={Mode=COMPLIANCE,Days=1}}'

aws s3api put-bucket-tagging --bucket "${S3_BUCKET2_NAME}" \
   --tagging="TagSet=[{Key=toDeleteAfterTests,Value=true}]"

popd
set +vex
echo "=========================== Finishing Create Worm Bucket Script =========================="
