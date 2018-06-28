#!/usr/bin/env bash
set -e  # exit if commands fails
set -x  # trace what gets exe

docker-compose ps
docker-compose kill
docker-compose rm -fv