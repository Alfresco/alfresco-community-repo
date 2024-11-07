#!/usr/bin/env bash

# This script checks the format and headers for Java files that have been modified compared to the provided branch.
# If no branch is provided, it defaults to origin/master.

set +x
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

# Accept branch name as a parameter, default to origin/master
branch_name=${1:-origin/master}

modified_files=$(git diff --name-only "$branch_name")

for file in ${modified_files}
do
  if [[ $file == *.java ]]; then
      include_list="${include_list},${file}"
    fi
done
include_list=${include_list:1}
echo $include_list

mvn spotless:apply validate -DlicenseUpdateHeaders=true -Pags,all-tas-tests -Dspotless-include-list="${include_list}" > /dev/null || true

popd
set -x
