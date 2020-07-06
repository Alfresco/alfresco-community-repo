#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

outputFile="$TRAVIS_BUILD_DIR/rm-automation/alfresco-content-services-installer-5.2.7.4-linux-x64.bin"
amzFile="/eu.dl.alfresco.com/release/enterprise/5.2/5.2.7/5.2.7.4/alfresco-content-services-installer-5.2.7.4-linux-x64.bin"
host="s3-eu-west-1.amazonaws.com"
contentType="binary/octet-stream"
dateValue=`TZ=GMT date -R`
stringToSign="GET\n\n${contentType}\n${dateValue}\n${amzFile}"
signature=`echo -en ${stringToSign} | openssl sha1 -hmac $RELEASE_AWS_SECRET_KEY -binary | base64`
curl -H "Host: ${host}" \
     -H "Date: ${dateValue}" \
     -H "Content-Type: ${contentType}" \
     -H "Authorization: AWS $RELEASE_AWS_ACCESS_KEY:${signature}" \
     https://${host}${amzFile} -o ${outputFile}
