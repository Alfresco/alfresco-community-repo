#!/usr/bin/env bash

set -e

HTTP_CREDENTIALS=$1
DEPENDENCIES_DIR="$(dirname "${BASH_SOURCE[0]}")"

mvn -f $DEPENDENCIES_DIR -B clean install

function clone_and_install {
  local project_path=$DEPENDENCIES_DIR/projects/$1
  if [ ! -d "$project_path" ]; then
    if [ -z "$HTTP_CREDENTIALS" ]; then
      git clone --single-branch --branch jakarta-migration git@github.com:Alfresco/$1.git $project_path
    else
      git clone --single-branch --branch jakarta-migration https://${HTTP_CREDENTIALS}@github.com/Alfresco/$1.git $project_path
    fi
  fi
  mvn -f $project_path -B clean install -DskipTests -Dmaven.javadoc.skip=true
}

clone_and_install surf-webscripts
clone_and_install alfresco-greenmail
clone_and_install alfresco-tas-utility
clone_and_install alfresco-tas-email
clone_and_install alfresco-office-services

#Ugly hack to deal with AOS<->REPO circular dependencies
mvn -f $DEPENDENCIES_DIR/.. install -pl :alfresco-remote-api -am -DskipTests -Dmaven.javadoc.skip=true
clone_and_install alfresco-aos-module

tomcat_image_path=$DEPENDENCIES_DIR/projects/alfresco-docker-base-tomcat
if [ ! -d "$tomcat_image_path" ]; then
  git clone --single-branch --branch jakarta-migration https://github.com/Alfresco/alfresco-docker-base-tomcat.git $tomcat_image_path
fi
docker build --build-arg JDIST=jre --build-arg DISTRIB_NAME=rockylinux --build-arg DISTRIB_MAJOR=8 --build-arg JAVA_MAJOR=17 --build-arg TOMCAT_MAJOR=10 -t tomcat10-jakarta $tomcat_image_path