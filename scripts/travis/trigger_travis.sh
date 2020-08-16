#!/usr/bin/env bash
set -ev

USER=${1}
REPO=${2}
BRANCH=${3}

if [ "${TRAVIS_PULL_REQUEST}" != "false" ]; then
  echo "Downstream projects shouldn't be triggered from PR builds"
  exit 1
fi

if ! git ls-remote --exit-code --heads "https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${USER}/${REPO}.git" "${BRANCH}" ; then
  echo "Branch \"${BRANCH}\" not found on the downstream repository ${USER}/${REPO}. Exiting..."
  exit 0
fi


URL="https://api.travis-ci.com/repo/${USER}%2F${REPO}/requests"
BODY="{
\"request\": {
  \"branch\":\"${BRANCH}\"
}}"

printf "Travis API call:\n  URL: %s\n  Body: %s\n\n" "${URL}" "${BODY}"

curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token ${TRAVIS_ACCESS_TOKEN_TEMP}" \
  -d "${BODY}" \
  "${URL}" \
 | tee /tmp/travis-request-output.txt

cat /tmp/travis-request-output.txt

if grep -q '"@type": "error"' /tmp/travis-request-output.txt; then
  echo "Error when triggering build..."
  exit 2
fi
if grep -q 'access denied' /tmp/travis-request-output.txt; then
  echo "Access denied when triggering build..."
  exit 3
fi

exit 0

