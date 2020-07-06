#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

outputFile=$HOME/rm-automation
amzFile="release/enterprise/5.2/5.2.7/5.2.7.4/alfresco-content-services-installer-5.2.7.4-linux-x64.bin"
region="eu-west-1"
bucket="eu.dl.alfresco.com"
resource="/${bucket}/${amzFile}"
contentType="binary/octet-stream"
dateValue=`TZ=GMT date -R`
stringToSign="GET\n\n${contentType}\n${dateValue}\n${resource}"
s3Key=$RELEASE_AWS_ACCESS_KEY
s3Secret=$RELEASE_AWS_SECRET_KEY
signature=`echo -en ${stringToSign} | openssl sha1 -hmac ${s3Secret} -binary | base64`
curl -H "Host: s3-${region}.amazonaws.com" \
     -H "Date: ${dateValue}" \
     -H "Content-Type: ${contentType}" \
     -H "Authorization: AWS ${s3Key}:${signature}" \
     https://s3-${region}.amazonaws.com/${bucket}/${amzFile} -o ${outputFile}
