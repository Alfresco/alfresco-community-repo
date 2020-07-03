#!/usr/bin/env bash

export PROFILE=$1
export INSTALLER=$2

cd rm-automation
mvn clean install -Pinstall-alfresco,${PROFILE} -Dinstaller.url=${INSTALLER}
