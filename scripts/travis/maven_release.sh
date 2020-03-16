#!/usr/bin/env bash
set -e

releaseVersion=$1
developmentVersion=$2
scm_path=$(mvn help:evaluate -Dexpression=project.scm.url -q -DforceStdout)

# Use full history for release
git checkout -B "${TRAVIS_BRANCH}"
# Add email to link commits to user
git config user.email "${GIT_EMAIL}"

if [ -z ${releaseVersion} ] || [ -z ${developmentVersion} ]; 
    then echo "Please provide a Release and Development verison in the format <acs-version>-<additional-info> (6.3.0-EA or 6.3.0-SNAPSHOT)"
         exit -1
else   
    mvn --batch-mode \
    -Dusername="${GIT_USERNAME}" \
    -Dpassword="${GIT_PASSWORD}" \
    -DreleaseVersion=${releaseVersion} \
    -DdevelopmentVersion=${developmentVersion} \
    -Dbuild-number=${TRAVIS_BUILD_NUMBER} \
    -Dbuild-name="${TRAVIS_BUILD_STAGE_NAME}" \
    -Dscm-path=${scm_path} \
    -DscmCommentPrefix="[maven-release-plugin][skip ci]" \
    -DskipTests \
    "-Darguments=-DskipTests -Dbuild-number=${TRAVIS_BUILD_NUMBER} '-Dbuild-name=${TRAVIS_BUILD_STAGE_NAME}' -Dscm-path=${scm_path} " \
    release:clean release:prepare release:perform \
    -Prelease
fi
