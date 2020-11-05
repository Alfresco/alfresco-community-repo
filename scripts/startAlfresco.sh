#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

cd $1
# if 2nd input parameter is true then use .env.ci where TRANSFORM_SERVICE_ENABLED flag is set to false
# in order to not use anymore Transform router and Shared File Store
if $2 ; then
  mv -u .env.ci .env
fi
docker-compose up -d
