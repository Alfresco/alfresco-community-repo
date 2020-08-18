#!/usr/bin/env bash

echo "=========================== Starting Cleanup Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vx
pushd "$(dirname "${BASH_SOURCE[0]}")/../"


# Stop and remove the containers
docker ps -a -q | xargs -l -r docker stop
docker ps -a -q | xargs -l -r docker rm

pip install awscli
printf "${CREATE_BUCKET_AWS_ACCESS_KEY}\n${CREATE_BUCKET_AWS_SECRET_KEY}\n\n\n" | aws configure

S3_BUCKET_NAME="travis-ags-${TRAVIS_JOB_NUMBER}"
S3_BUCKET2_NAME="travis-ags-worm-${TRAVIS_JOB_NUMBER}-b2"

aws s3 ls | awk '{print $3}' | grep "^${S3_BUCKET_NAME}" | xargs -l -r -I{} aws s3 rb "s3://{}" --force
aws s3 ls | awk '{print $3}' | grep "^${S3_BUCKET2_NAME}" | xargs -l -r -I{} aws s3 rb "s3://{}" --force

popd
set +vx
echo "=========================== Finishing Cleanup Script =========================="