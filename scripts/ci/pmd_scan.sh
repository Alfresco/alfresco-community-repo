#!/usr/bin/env bash
echo "=========================== Starting PMD Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex

target_ref=$1
head_ref=$2

# Requires pmd/pmd-github-action to have been executed already, as this will download PMD.
runPMD="/opt/hostedtoolcache/pmd/${PMD_VERSION}/x64/pmd-bin-${PMD_VERSION}/bin/run.sh"

# Make a copy of the ruleset so that we ignore any changes between commits.
cp pmd-ruleset.xml /tmp/pmd-ruleset.xml

# Create a list of the files changed by this PR.
baseline_ref=$(git merge-base "${target_ref}" "${head_ref}")
git diff --name-only ${baseline_ref} ${head_ref} > /tmp/file-list.txt

# Run PMD against the baseline commit.
git checkout ${baseline_ref}
for file in $(cat /tmp/file-list.txt)
do
    if [[ -f ${file} ]]
    then
        echo ${file} > /tmp/old-files.txt
    fi
done
${runPMD} pmd --cache pmd.cache --file-list /tmp/old-files.txt -R /tmp/pmd-ruleset.xml -r old_report.txt --fail-on-violation false
old_issue_count=$(cat old_report.txt | wc -l)

# Rerun PMD against the PR head commit.
git checkout ${head_ref}
for file in $(cat /tmp/file-list.txt)
do
    if [[ -f ${file} ]]
    then
        echo ${file} > /tmp/new-files.txt
    fi
done
${runPMD} pmd --cache pmd.cache --file-list /tmp/new-files.txt -R /tmp/pmd-ruleset.xml -r new_report.txt --fail-on-violation false
new_issue_count=$(cat new_report.txt | wc -l)

# Display the differences between the two files.
diff old_report.txt new_report.txt | true

# Fail the build if there are more issues now than before.
if [[ ${new_issue_count} > ${old_issue_count} ]];
then
    echo "ERROR: Number of PMD issues in edited files increased from ${old_issue_count} to ${new_issue_count}"
    exit 1
else
    echo "Number of PMD issues in edited files went from ${old_issue_count} to ${new_issue_count}"
fi

set +vex
echo "=========================== Finishing PMD Script =========================="
