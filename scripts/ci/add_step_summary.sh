#!/usr/bin/env bash

echo "=========================== Starting Add Step Summary Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex

echo "#### ⏱ Before Tests: $(date -u +'%Y-%m-%d %H:%M:%S%:z')" >> $GITHUB_STEP_SUMMARY
echo "#### ⚙ Configuration" >> $GITHUB_STEP_SUMMARY

if [[ "$RP_ENABLED" == 'true' ]]; then
  echo "- [Report Portal]($RP_URL) configured with key "'`'$RP_KEY'`' >> $GITHUB_STEP_SUMMARY
else
  echo "- Report Portal not enabled" >> $GITHUB_STEP_SUMMARY
fi

set +vex
echo "=========================== Finishing Add Step Summary Script =========================="
