#!/usr/bin/env bash

TAS_DIRECTORY=$1

cd ${TAS_DIRECTORY}

# Add command to list all files in /target directory
echo "Listing all files in /target directory:"
ls -l target/

echo "" # This adds an empty line for clarity

# Add command to list all files in /target/surefire-reports directory
echo "Listing all files in /target/surefire-reports directory:"
ls -l target/surefire-reports/

echo "" # This adds an empty line for clarity

# Add command to list all files in /target/surefire-reports/testng-junit-results directory
echo "Listing all files in /target/surefire-reports/testng-junit-results directory:"
ls -l target/surefire-reports/testng-junit-results

echo "" # This adds an empty line for clarity

# Add command to list all files in /target/surefire-reports/testng-junit-results/Surefire suite directory
echo "Listing all files in /target/surefire-reports/testng-junit-results/Surefire suite directory:"
ls -l "target/surefire-reports/testng-junit-results/Surefire suite"

echo "" # This adds an empty line for clarity

echo "Print : Surefire test.xml"
cat "target/surefire-reports/testng-junit-results/Surefire suite/Surefire test.xml"

echo "" # This adds an empty line for clarity

# Add command to list all files in /target/surefire-reports/testng-native-results directory
echo "Listing all files in /target/surefire-reports/testng-native-results directory:"
ls -l target/surefire-reports/testng-native-results

echo "" # This adds an empty line for clarity

# Add command to list all files in /target/surefire-reports/testng-native-results/Surefire suite directory
echo "Listing all files in /target/surefire-reports/testng-native-results/Surefire suite directory:"
ls -l "target/surefire-reports/testng-native-results/Surefire suite"

echo "" # This adds an empty line for clarity

echo "Print : Surefire test.xml"
cat "target/surefire-reports/testng-native-results/Surefire suite/Surefire test.xml"

echo "" # This adds an empty line for clarity

rm -rf target/surefire-reports/testng-native-results

echo "" # This adds an empty line for clarity

echo "Listing all files in /target/surefire-reports directory:"
ls -l target/surefire-reports

failures=$(grep 'status="FAIL"' target/surefire-reports/testng-results.xml | sed 's|^.*[ ]name="\([^"]*\)".*$|\1|g')

for failure in ${failures}
do
    cat target/reports/alfresco-tas.log | sed '/STARTING Test: \['${failure}'\]/,/ENDING Test: \['${failure}'\]/!d;/ENDING Test: \['${failure}'\]/q'
done
