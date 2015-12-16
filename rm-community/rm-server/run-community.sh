#!/bin/bash
# Downloads the spring-loaded lib if not existing and runs repository AMP
springloadedfile=~/.m2/repository/org/springframework/springloaded/1.2.0.RELEASE/springloaded-1.2.0.RELEASE.jar

if [ ! -f $springloadedfile ]; then
mvn validate -Psetup
fi

MAVEN_OPTS="-javaagent:$springloadedfile -noverify -Xms256m -Xmx2G -XX:PermSize=300m" mvn clean integration-test -PappWarLocationSetting,fullBuild,amp-with-solr -Dsolr.secureComms=none