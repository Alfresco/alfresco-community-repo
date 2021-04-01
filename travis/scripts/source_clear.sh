#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

mvn -B -q clean install \
    -DskipTests \
    -Dmaven.javadoc.skip=true \
    -pl '!rm-automation,!rm-automation/rm-automation-community-rest-api,!rm-automation/rm-automation-enterprise-rest-api,!rm-automation/rm-automation-ui,!rm-benchmark' \
    com.srcclr:srcclr-maven-plugin:scan \
    -Dcom.srcclr.apiToken=$SRCCLR_API_TOKEN > scan.log

SUCCESS=$?   # this will read exit code of the previous command

cat scan.log | grep -e 'Full Report Details' -e 'Failed'

exit ${SUCCESS}
