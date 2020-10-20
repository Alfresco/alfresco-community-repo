#!/usr/bin/env bash
set -ev

echo "Travis commit message: $TRAVIS_COMMIT_MESSAGE"
release_message=$(echo $TRAVIS_COMMIT_MESSAGE | grep -Po '(\[(community|enterprise)\srelease\s(\d\.)+(\d|[a-z])(-[A-Z]\d){0,1}\s(\d\.)+\d-SNAPSHOT\])')

if [ ! -n "$release_message" ]; then
  echo "The commit message is in the wrong format or it does not contain all the required properties."
  exit 1
fi

export RELEASE_VERSION=$(echo $release_message | grep -Po '(\d\.)+(\d|[a-z])(-[A-Z]\d){0,1}' | head -1)
export DEVELOPMENT_VERSION=$(echo $release_message | grep -Po '(\d\.)+\d-SNAPSHOT')

echo "Release version is set to $RELEASE_VERSION"
echo "Development version is set to $DEVELOPMENT_VERSION"