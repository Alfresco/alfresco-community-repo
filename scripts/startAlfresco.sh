#!/usr/bin/env bash

export PROFILE=$1

cd rm-automation
mvn clean install -Pinstall-alfresco,${PROFILE} -Dinstaller.url=${env.INSTALLER_URL}
