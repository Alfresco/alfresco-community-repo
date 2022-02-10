#!/usr/bin/env bash

echo "=========================== Starting SourceClear Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set +e -v -x
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

mvn -B -q clean install \
    -DskipTests \
    -Dmaven.javadoc.skip=true \
    com.srcclr:srcclr-maven-plugin:scan \
    -Dcom.srcclr.apiToken=${SRCCLR_API_TOKEN} > scan.log

SUCCESS=$?   # this will read exit code of the previous command

if [ -z "$VERACODE_FAILS_BUILD" ] || [ "$VERACODE_FAILS_BUILD" = false ] ; then
    SUCCESS=0
fi

cat scan.log | grep -e 'Full Report Details' -e 'Failed'

popd
set +vex
echo "=========================== Finishing SourceClear Script =========================="

exit ${SUCCESS}