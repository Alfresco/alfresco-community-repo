#!/usr/bin/env bash

echo "=========================== Starting MB Env Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"


bash scripts/start-compose.sh "${PWD}/${ENTERPRISE_SHARE_PATH}/worm-support-docker-compose.yml"

popd
set +vex
echo "=========================== Finishing MB Env Script =========================="
