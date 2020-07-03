#!/usr/bin/env bash

export PROFILE=$1

cd rm-automation
mvn install -Pinstall-alfresco,${PROFILE} -Dinstaller.url=${INSTALLER_URL}
