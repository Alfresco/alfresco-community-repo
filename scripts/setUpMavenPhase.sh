#!/usr/bin/env bash
echo "Branch name: ${TRAVIS_BRANCH}"

if [ "${TRAVIS_BRANCH}" == "master" ];
then
    export mavenPhase="deploy"
elif [[ ${TRAVIS_BRANCH} = release* ]];
then
    export mavenPhase="deploy"
else
    export mavenPhase="verify"
fi
echo "Maven Phase: ${mavenPhase}"