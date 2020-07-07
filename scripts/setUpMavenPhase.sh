#!/usr/bin/env bash
echo "Branch name: ${TRAVIS_BRANCH}"
echo "Branch name: ${TRAVIS_PULL_REQUEST}"

if [ "${TRAVIS_BRANCH}" == "master" ];
then
    export MAVEN_PHASE="deploy"
elif [[ ${TRAVIS_BRANCH} = release* ]];
then
    export MAVEN_PHASE="deploy"
else
    export MAVEN_PHASE="verify"
fi

# If this is pull request
if [ "${TRAVIS_PULL_REQUEST}" != "false" ];
then
  export MAVEN_PHASE="verify"
fi


