#!/usr/bin/env bash
CONTAINER_NAME=alfresco-db-mysql

MYSQL_USER=alfresco
MYSQL_ROOT_PASSWORD=alfresco
MYSQL_DATABASE=alfresco

MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306

usage () {
    echo "Usage: $0 <image>"
}

if [[ $# -ne 1 ]]; then
    usage
    exit 1
fi