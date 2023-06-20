#!/usr/bin/env bash

target_ref=$1
head_ref=$2

# Requires pmd/pmd-github-action to have been executed already, as this will download PMD and set up the changed file list.
runPMD="/opt/hostedtoolcache/pmd/${PMD_VERSION}/x64/pmd-bin-${PMD_VERSION}/bin/run.sh"

# Run PMD against the baseline commit.
baseline_ref=$(git merge-base "${target_ref}" "${head_ref}")
git checkout ${baseline_ref}
${runPMD} pmd --cache pmd.cache --file-list pmd.filelist -R pmd-ruleset.xml -r old_report.txt
old_issue_count=$(cat old_report.txt | wc -l)

# Rerun PMD against the PR head commit.
git checkout ${head_ref}
${runPMD} pmd --cache pmd.cache --file-list pmd.filelist -R pmd-ruleset.xml -r new_report.txt
new_issue_count=$(cat new_report.txt | wc -l)

# Display the differences between the two files.
diff old_report.txt new_report.txt

# Fail the build if there are more issues now than before.
if [[ ${new_issue_count} > ${old_issue_count} ]];
then
    echo "ERROR: Number of PMD issues in edited files increased from ${old_issue_count} to ${new_issue_count}"
    exit 1
else
    echo "Number of PMD issues in edited files went from ${old_issue_count} to ${new_issue_count}"
fi
