#!/usr/bin/env bash
set -e

# Use full history for release
git checkout -B "${TRAVIS_BRANCH}"
# Add email to link commits to user
git config user.email "${GIT_COMMITTER_EMAIL}"
git config user.name "${GIT_COMMITTER_NAME}"

if [ -z ${RELEASE_VERSION} ] || [ -z ${DEVELOPMENT_VERSION} ];
    then echo "Please provide a Release and Development verison"
         exit -1
else
    mvn --batch-mode
    -Dusername="${GITHUB_USERNAME}" \
    -Dpassword="${GITHUB_PASSWORD}" \
    -DreleaseVersion=${RELEASE_VERSION} \
    -DdevelopmentVersion=${DEVELOPMENT_VERSION} \
    -DskipTests -Dcommunity -DuseReleaseProfile=false \
    -Prelease-community release:clean release:prepare release:perform
fi
