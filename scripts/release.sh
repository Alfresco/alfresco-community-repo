#!/usr/bin/env bash
set -e

# Use full history for release
git checkout -B "${TRAVIS_BRANCH}"

git config user.email "build@alfresco.com"

release_type=$1
echo Release type: "$release_type"

if [ -z $release_type ]; then
    echo "Please provide a release type."
    exit 1
elif [ $release_type != "community" -a $release_type != "enterprise" ]; then
    echo "The provided release type is not valid."
    exit 1
fi

if [ -z ${RELEASE_VERSION} ] || [ -z ${DEVELOPMENT_VERSION} ]; then
    echo "Please provide a Release and Development verison"
    exit 1
fi

# Docker Logins
echo "${DOCKERHUB_PASSWORD}" | docker login -u="${DOCKERHUB_USERNAME}" --password-stdin
echo "${QUAY_PASSWORD}" | docker login -u="${QUAY_USERNAME}" --password-stdin quay.io

# Check if it's a hotfix version by counting the number of dots in the version number.
if [ $(echo "${RELEASE_VERSION}" | grep -o "\." | wc -l) == 3 ] && [ ${release_type} == "enterprise" ];
then
  deployment_repository="hotfix-release"
else
  deployment_repository="${release_type}-release"
fi

mvn --batch-mode \
    -Dusername="${GIT_USERNAME}" \
    -Dpassword="${GIT_PASSWORD}" \
    -DreleaseVersion=${RELEASE_VERSION} \
    -DdevelopmentVersion=${DEVELOPMENT_VERSION} \
    -DscmCommentPrefix="[maven-release-plugin][skip ci] " \
    -DuseReleaseProfile=false \
    "-Darguments=-DskipTests -D${release_type} -P${deployment_repository},release-${release_type}" \
    release:clean release:prepare release:perform