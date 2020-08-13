#!/usr/bin/env bash

echo "=========================== Starting Init Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../"

mkdir -p ${HOME}/.m2 && cp -rf _ci/settings.xml ${HOME}/.m2/
echo "${QUAY_PASSWORD}" | docker login -u="${QUAY_USERNAME}" --password-stdin quay.io
find "${HOME}/.m2/repository/" -type d -name "*-SNAPSHOT*" | xargs -r -l rm -rf

# Enable experimental docker features (e.g. squash options)
echo '{"experimental":true}' | sudo tee /etc/docker/daemon.json
sudo service docker restart

popd
set +vex
echo "=========================== Finishing Init Script =========================="