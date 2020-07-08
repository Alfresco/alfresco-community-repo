#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

outputFile="$TRAVIS_BUILD_DIR/$1/alf-installer.bin"
host="s3-eu-west-1.amazonaws.com"
contentType="binary/octet-stream"
dateValue=`TZ=GMT date -R`
stringToSign="GET\n\n${contentType}\n${dateValue}\n$S3_INSTALLER_PATH"
signature=`echo -en ${stringToSign} | openssl sha1 -hmac $RELEASE_AWS_SECRET_KEY -binary | base64`
curl -H "Host: ${host}" \
     -H "Date: ${dateValue}" \
     -H "Content-Type: ${contentType}" \
     -H "Authorization: AWS $RELEASE_AWS_ACCESS_KEY:${signature}" \
     https://${host}${S3_INSTALLER_PATH} -o ${outputFile}

ls "$TRAVIS_BUILD_DIR/$1"
