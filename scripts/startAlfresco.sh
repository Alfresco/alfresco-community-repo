#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

export PROFILE=$1

ls rm-enterprise/rm-enterprise-repo/target
ls rm-enterprise/rm-enterprise-share/target
cd rm-automation
ls
mvn install -Pinstall-alfresco,${PROFILE} -q
