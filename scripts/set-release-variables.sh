#!/bin/bash
set -e

# TODO: check if the message is in the right format eg [internal community release 3.4.0 3.4.0-SNAPSHOT]


export RELEASE_VERSION=$(echo $TRAVIS_COMMIT_MESSAGE | grep -Po '(\d\.)+\d ')
export DEVELOPMENT_VERSION=$(echo $TRAVIS_COMMIT_MESSAGE | grep -Po '(\d\.)+\d-SNAPSHOT')

export release_type=$(echo $TRAVIS_COMMIT_MESSAGE | grep -Po 'internal (community|enterprise)')

if [[ $release_type =~ "community" ]]; then
  echo "Setting Community release variables..."
  if [[ $release_type =~ "internal" ]]; then
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the Internal release"
    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging"
    export ARTIFACTS_UPLOAD_DIR="community/alfresco-governance-services/release/${TRAVIS_BRANCH}"
  else
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the release"
#    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging" ->
#    export ARTIFACTS_UPLOAD_DIR="community/alfresco-governance-services/release/${TRAVIS_BRANCH}"
  fi
elif [[ $release_type =~ "enterprise" ]]; then
   echo "Setting Enterprise release variables..."
  if [[ $release_type =~ "internal" ]]; then
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the Internal release"
    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging"
    export ARTIFACTS_UPLOAD_DIR="enterprise/alfresco-governance-services/release/${TRAVIS_BRANCH}"
  else
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the release"
#    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging" ->
#    export ARTIFACTS_UPLOAD_DIR="enterprise/alfresco-governance-services/release/${TRAVIS_BRANCH}"
  fi
fi