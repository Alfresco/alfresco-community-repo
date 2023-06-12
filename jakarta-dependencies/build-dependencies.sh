#!/usr/bin/env bash

set -e

DEPENDENCIES_DIR="$(dirname "${BASH_SOURCE[0]}")"

mvn -f $DEPENDENCIES_DIR -B clean install

function clone_and_install {
  local project_path=$DEPENDENCIES_DIR/projects/$1
  if [ ! -d "$project_path" ]; then
    git clone --single-branch --branch jakarta-migration git@github.com:Alfresco/$1.git $project_path
  fi
  mvn -f $project_path -B clean install
}

clone_and_install surf-webscripts
clone_and_install alfresco-greenmail
clone_and_install alfresco-tas-email