#!/bin/bash
set -xe
DIRECTORY=`dirname $0`
echo $DIRECTORY
 $(dirname "${BASH_SOURCE[0]}")/common.sh

echo "============================================"
echo "Generate custom config file"
echo "============================================"
rm -rf /tmp/Docker
mkdir -p /tmp/Docker

echo "
[mysqld]
collation-server = utf8_unicode_ci
character-set-server = utf8
innodb_locks_unsafe_for_binlog = 1
default_storage_engine = InnoDB
max_connections = 275
lock_wait_timeout = 5
innodb_buffer_pool_size = 1G
innodb_additional_mem_pool_size = 16M
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M
innodb_data_file_path = ibdata1:64M:autoextend
innodb_autoextend_increment = 64
init_file=/etc/mysql/conf.d/init-file.sql
" > /tmp/Docker/config-file.cnf

echo "
GRANT ALL on $MYSQL_USER.* to '$MYSQL_DATABASE'@'%' identified by '$MYSQL_ROOT_PASSWORD' with grant option;
FLUSH HOSTS;
FLUSH PRIVILEGES;
" > /tmp/Docker/init-file.sql

echo "============================================"
echo "Pulling and Running $1"
echo "============================================"
sg docker "docker run -p $MYSQL_PORT:3306 -v /tmp/Docker:/etc/mysql/conf.d --name $CONTAINER_NAME -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD -e MYSQL_USER=$MYSQL_USER -e MYSQL_DATABASE=$MYSQL_DATABASE -d $1"

sleep 120

# The init_file option should be enough, but some old version of MariaDB do not honor it...
docker exec -t $CONTAINER_NAME mysql -v -h $MYSQL_HOST --port=$MYSQL_PORT -u root --password=$MYSQL_ROOT_PASSWORD -e "source /etc/mysql/conf.d/init-file.sql"