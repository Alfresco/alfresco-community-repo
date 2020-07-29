#!/usr/bin/env bash

USER=${1}
REPO=${2}
BRANCH=${3}

if ! git ls-remote --exit-code --heads "https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${USER}/${REPO}.git" "${BRANCH}" ; then
  echo "Branch \"${BRANCH}\" not found on the downstream repository ${USER}/${REPO}. Exiting..."
  exit 0
fi


body="{
\"request\": {
  \"branch\":\"${BRANCH}\"
}}"

curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token ${TRAVIS_ACCESS_TOKEN}" \
  -d "${body}" \
  "https://api.travis-ci.com/repo/${USER}%2F${REPO}/requests" \
 | tee /tmp/travis-request-output.txt

if grep -q '"@type": "error"' /tmp/travis-request-output.txt; then
    exit 1
fi
if grep -q 'access denied' /tmp/travis-request-output.txt; then
    exit 1
fi