#!/usr/bin/env bash

echo "Travis commit message: $TRAVIS_COMMIT_MESSAGE"
release_message=$(echo $TRAVIS_COMMIT_MESSAGE | grep -Po '(\[(internal )*(community|enterprise)\srelease\s(\d\.)+(\d|[a-z])(-[A-Z]\d){0,1}\s(\d\.)+\d-SNAPSHOT\])')

if [ ! -n "$release_message" ]; then
  echo "The commit message is in the wrong format or it does not contain all the required properties."
  exit 1
fi

export RELEASE_VERSION=$(echo $release_message | grep -Po '(\d\.)+(\d|[a-z])(-[A-Z]\d){0,1}' | head -1)
export DEVELOPMENT_VERSION=$(echo $release_message | grep -Po '(\d\.)+\d-SNAPSHOT')

echo "Release version is set to $RELEASE_VERSION"
echo "Development version is set to $DEVELOPMENT_VERSION"

release_type=$(echo $release_message | grep -Po '(internal\s)*(community|enterprise)')

if [[ $release_type =~ "community" ]]; then
  echo "Setting Community Release variables..."
  export RELEASE_TYPE="community"
  if [[ $release_type =~ "internal" ]]; then
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the Internal release"
    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging"
    export ARTIFACTS_UPLOAD_DIR="community/RM/${RELEASE_VERSION}"
  else
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the release"
    export ARTIFACTS_UPLOAD_BUCKET="eu.dl.alfresco.com"
    export ARTIFACTS_UPLOAD_DIR="release/community/RM/${RELEASE_VERSION}"
  fi
elif [[ $release_type =~ "enterprise" ]]; then
  echo "Setting Enterprise Release variables..."
  export RELEASE_TYPE="enterprise"
  if [[ $release_type =~ "internal" ]]; then
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the Internal release"
    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging"
    export ARTIFACTS_UPLOAD_DIR="enterprise/RM/${RELEASE_VERSION}"
  else
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the release"
    export ARTIFACTS_UPLOAD_BUCKET="eu.dl.alfresco.com"
    export ARTIFACTS_UPLOAD_DIR="release/enterprise/RM/${RELEASE_VERSION}"
  fi
fi