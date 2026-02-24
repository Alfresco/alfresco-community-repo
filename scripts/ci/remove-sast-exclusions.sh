#!/usr/bin/env bash
echo "=========================== Excluding Files from Veracode SAST ==========================="
set -ex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

# Copy war file to temporary directory
cp -f "$1" "$2"

# Remove files to be excluded from Veracode SAST
exclusions="./scripts/ci/SAST-exclusion-list.txt"
if [ -e $exclusions ]
then
    while read -r line
    do
      echo "Removing WEB-INF/lib/$line"
      zip -d "$2" "WEB-INF/lib/$line" || true
    done < "$exclusions"
else
    echo "No files to be excluded from SAST"
fi

popd
set +ex
echo "=========================== Finishing Excluding Files from Veracode SAST =========================="
