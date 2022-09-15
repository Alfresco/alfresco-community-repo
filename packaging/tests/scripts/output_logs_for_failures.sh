#!/usr/bin/env bash

TAS_DIRECTORY=$1

cd ${TAS_DIRECTORY}

failures=$(grep 'status="FAIL"' target/surefire-reports/testng-results.xml | sed 's|^.*[ ]name="\([^"]*\)".*$|\1|g')

for failure in ${failures}
do
    cat target/reports/alfresco-tas.log | sed '/STARTING Test: \['${failure}'\]/,/ENDING Test: \['${failure}'\]/!d;/ENDING Test: \['${failure}'\]/q'
done
