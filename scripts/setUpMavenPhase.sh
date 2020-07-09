#!/usr/bin/env bash
echo "Branch name: ${TRAVIS_BRANCH}"
echo "Pull request: ${TRAVIS_PULL_REQUEST}"
echo "Travis job name: ${TRAVIS_JOB_NAME}"
echo "Travis job id: ${TRAVIS_JOB_ID}"
branchName=${TRAVIS_BRANCH}
imageTag=${branchName:8}
echo "Image tag: ${imageTag}"

case ${TRAVIS_JOB_NAME} in
"Build AGS Community"|"Build AGS Benchmark" )
    if [[ "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_PULL_REQUEST}" == "false" ]];
    then
        export MAVEN_PHASE="deploy"
        export BUILD_PROFILE="master"
        export IMAGE_TAG="latest"
    elif [[ ${TRAVIS_BRANCH} = release*  && "${TRAVIS_PULL_REQUEST}" == "false" ]];
    then
        export MAVEN_PHASE="deploy"
        export BUILD_PROFILE="master"
        export IMAGE_TAG="${imageTag}-latest"
    else
        export MAVEN_PHASE="verify"
        export BUILD_PROFILE="buildDockerImage"
        export IMAGE_TAG="latest"
    fi
    ;;
"Build AGS Enterprise" )
   if [[ "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_PULL_REQUEST}" == "false" ]];
    then
        export MAVEN_PHASE="deploy"
        export BUILD_PROFILE="internal"
        export IMAGE_TAG="latest"
    elif [[ ${TRAVIS_BRANCH} = release*  && "${TRAVIS_PULL_REQUEST}" == "false" ]];
    then
        export MAVEN_PHASE="deploy"
        export BUILD_PROFILE="internal"
        export IMAGE_TAG="${imageTag}-latest"
    else
        export MAVEN_PHASE="verify"
        export BUILD_PROFILE="buildDockerImage"
        export IMAGE_TAG="latest"
    fi
    ;;
esac