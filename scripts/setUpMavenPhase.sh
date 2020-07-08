#!/usr/bin/env bash
echo "Branch name: ${TRAVIS_BRANCH}"
echo "Pull request: ${TRAVIS_PULL_REQUEST}"

if [[ "${TRAVIS_BRANCH}" == "master" || "${TRAVIS_BRANCH}" = release* ]] && [ "${TRAVIS_PULL_REQUEST}" == "false" ];
then
    export MAVEN_PHASE="deploy"
else
    export MAVEN_PHASE="deploy"
fi

