#!/usr/bin/env bash

if [[ -z ${GITHUB_MODIFIED_FILES} ]]
then
  modified_files=$(git diff --cached --name-only --diff-filter=ACMR)
else
  modified_files=${GITHUB_MODIFIED_FILES}
fi

include_list=""
for file in ${modified_files}
do
  include_list="${include_list},${file}"
done
include_list=${include_list:1}

mvn spotless:apply validate -DlicenseUpdateHeaders=true -Dspotless-include-list="${include_list}" > /dev/null

all_nonconformant_files=$(git diff --name-only --diff-filter=ACMR)

for file in ${all_nonconformant_files}
do
  revert=1
  for modified_file in ${modified_files}
  do
    if [[ "${modified_file}" == "${file}" ]]
    then
      revert=0
      break
    fi
  done
  if [[ ${revert} == 1 ]]
  then
    git checkout -- "${file}"
  fi
done
