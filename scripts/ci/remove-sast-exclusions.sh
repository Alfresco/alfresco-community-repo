#!/usr/bin/env bash
echo "=========================== Excluding Files from Veracode SAST ==========================="
set -ex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

# Copy alfresco.war file
/bin/cp -f ./packaging/war/target/alfresco.war ./scripts/ci/alfresco-reduced.war

# Remove files to be excluded from Veracode SAST
exclusions="./scripts/ci/SAST-exclusion-list.txt"
if [ -e $exclusions ]
then
    while read -r line
    do
      echo "Removing WEB-INF/lib/$line"
      zip -d ./scripts/ci/alfresco-reduced.war "WEB-INF/lib/$line" || true
    done < "$exclusions"
else
    echo "No files to be excluded from SAST"
fi

popd
set +ex
echo "=========================== Finishing Excluding Files from Veracode SAST =========================="