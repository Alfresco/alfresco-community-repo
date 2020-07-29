#!/usr/bin/env bash
set -ev

RELEASE_VERSION=${1}
DEVELOPMENT_VERSION=${2}
SCM_PATH=$(mvn help:evaluate -Dexpression=project.scm.url -q -DforceStdout)

# Use full history for release
git checkout -B "${TRAVIS_BRANCH}"
# Add email to link commits to user
git config user.email "${GIT_EMAIL}"

if [ -z "${RELEASE_VERSION}" ] || [ -z "${DEVELOPMENT_VERSION}" ]; then
  echo "Please provide a Release and Development version in the format <acs-version>-<additional-info> (6.3.0-EA or 6.3.0-SNAPSHOT)"
  exit 1
fi

mvn -B \
  -PfullBuild,all-tas-tests \
  -Prelease \
  -DskipTests \
  -Dusername="${GIT_USERNAME}" \
  -Dpassword="${GIT_PASSWORD}" \
  -DreleaseVersion="${RELEASE_VERSION}" \
  -DdevelopmentVersion="${DEVELOPMENT_VERSION}" \
  -Dbuild-number="${TRAVIS_BUILD_NUMBER}" \
  -Dbuild-name="${TRAVIS_BUILD_STAGE_NAME}" \
  -Dscm-path="${SCM_PATH}" \
  -DscmCommentPrefix="[maven-release-plugin][skip ci] " \
  "-Darguments=-DskipTests -Dbuild-number=${TRAVIS_BUILD_NUMBER} '-Dbuild-name=${TRAVIS_BUILD_STAGE_NAME}' -Dscm-path=${SCM_PATH} -PfullBuild,all-tas-tests" \
  release:clean release:prepare release:perform

