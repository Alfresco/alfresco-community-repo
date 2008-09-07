#!/bin/sh

export CLASSPATH=./cmis-test-client.jar
for jar in ./lib/*.jar; do export CLASSPATH=$CLASSPATH:$jar; done

java org.alfresco.cmis.ws.example.SimpleExecutableCmisServicesUtilizer $1 $2 $3
