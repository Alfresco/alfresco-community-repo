#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

cd $1
docker login quay.io -u ${QUAY_USERNAME} -p ${QUAY_PASSWORD}
docker-compose up -d
cd $TRAVIS_BUILD_DIR
