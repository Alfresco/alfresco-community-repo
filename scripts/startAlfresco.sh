#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

cd $1
# if 2nd input parameter is true then use legacy transformers
# (flags LOCAL_TRANSFORM_SERVICE_ENABLED and TRANSFORM_SERVICE_ENABLED are set to false in .env.ci )
if $2 ; then
  mv -u .env.ci .env
fi
docker-compose up -d
