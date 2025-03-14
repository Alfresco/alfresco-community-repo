#!/usr/bin/env bash

set +x

mvn spotless:apply validate -DlicenseUpdateHeaders=true -Pags,all-tas-tests > /dev/null || true

set -x
