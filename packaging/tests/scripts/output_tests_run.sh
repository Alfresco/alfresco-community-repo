#!/usr/bin/env bash

TAS_DIRECTORY=$1

cd ${TAS_DIRECTORY}

cat target/reports/alfresco-tas.log
cat target/reports/alfresco-tas.log | grep -a "*** STARTING"