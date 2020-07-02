#!/usr/bin/env bash
echo "Branch name: ${TRAVIS_BRANCH}"

if [ "${TRAVIS_BRANCH}" == "master" ];
then
    export MAVEN_PHASE="deploy"
elif [[ ${TRAVIS_BRANCH} = release* ]];
then
    export MAVEN_PHASE=$"deploy"
else
    export MAVEN_PHASE=$"verify"
fi
