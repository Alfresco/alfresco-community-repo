#!/usr/bin/env bash

release_message=$(echo $TRAVIS_COMMIT_MESSAGE | ggrep -Po '\[(internal )*(community|enterprise)\srelease\s(\d\.)+(\d|[a-z])\s(\d\.)+\d-SNAPSHOT\]')

if [ ! -n "$release_message" ]; then
  echo "The commit message is in the wrong format or it does not contain all the required properties."
  exit 0
fi

export RELEASE_VERSION=$(echo $release_message | ggrep -Po '\g<1>(\d\.)+(\d|[a-z])')
export DEVELOPMENT_VERSION=$(echo $release_message | ggrep -Po '(\d\.)+\d-SNAPSHOT')

echo "Release version is set to $RELEASE_VERSION"
echo "Development version is set to $DEVELOPMENT_VERSION"

release_type=$(echo $release_message | ggrep -Po '(internal\s)*(community|enterprise)')

if [[ $release_type =~ "community" ]]; then
  echo "Setting Community Release variables..."
  if [[ $release_type =~ "internal" ]]; then
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the Internal release"
    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging"
    export ARTIFACTS_UPLOAD_DIR="community/alfresco-governance-services/release/${TRAVIS_BRANCH}"
  else
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the release"
    export ARTIFACTS_UPLOAD_BUCKET="eu.dl.alfresco.com"
    export ARTIFACTS_UPLOAD_DIR="release/community/RM/${RELEASE_VERSION}"
  fi
elif [[ $release_type =~ "enterprise" ]]; then
  echo "Setting Enterprise Release variables..."
  if [[ $release_type =~ "internal" ]]; then
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the Internal release"
    export ARTIFACTS_UPLOAD_BUCKET="alfresco-artefacts-staging"
    export ARTIFACTS_UPLOAD_DIR="enterprise/alfresco-governance-services/release/${TRAVIS_BRANCH}"
  else
    echo "Setting ARTIFACTS_UPLOAD_BUCKET and ARTIFACTS_UPLOAD_DIR for the release"
    export ARTIFACTS_UPLOAD_BUCKET="eu.dl.alfresco.com"
    export ARTIFACTS_UPLOAD_DIR="release/enterprise/RM/${RELEASE_VERSION}"
  fi
fi