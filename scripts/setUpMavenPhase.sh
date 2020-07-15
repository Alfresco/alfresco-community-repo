#!/usr/bin/env bash
echo "Branch name: ${TRAVIS_BRANCH}"
echo "Pull request: ${TRAVIS_PULL_REQUEST}"
echo "Travis job name: ${TRAVIS_JOB_NAME}"
branchName=${TRAVIS_BRANCH}
imageTag=${branchName:8}
echo "Image tag: ${imageTag}"

  if [[ ${TRAVIS_JOB_NAME} == "Build AGS Enterprise" ]];
  then
     export BUILD_PROFILE="internal"
  else
     export BUILD_PROFILE="master"
  fi

  if [[ "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_PULL_REQUEST}" == "false" ]];
  then
      export MAVEN_PHASE="deploy"
      export IMAGE_TAG="latest"
  elif [[ ${TRAVIS_BRANCH} = release*  && "${TRAVIS_PULL_REQUEST}" == "false" ]];
  then
      export MAVEN_PHASE="deploy"
      export IMAGE_TAG="${imageTag}-latest"
  else
      export MAVEN_PHASE="verify"
      export BUILD_PROFILE="buildDockerImage"
      export IMAGE_TAG="latest"
  fi
