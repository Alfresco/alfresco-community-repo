#!/bin/bash
# -------
# Script for apply AMPs to installed WAR
# -------
pushd $(dirname $0)>/dev/null
export SCRIPTPATH=$(pwd)
export ALF_HOME=${SCRIPTPATH%/*}
export CATALINA_HOME=$ALF_HOME/tomcat
required_java_version="1.7"

if [ -f $CATALINA_HOME/bin/setenv.sh ]; then
  . $CATALINA_HOME/bin/setenv.sh
fi

# Verify Java installation into ALF_HOME folder
if [ -f $ALF_HOME/java/bin/java ] && [ -x $ALF_HOME/java/bin/java ]; then
    echo
    echo "Found java executable in $ALF_HOME/java"
    _java=$ALF_HOME/java/bin/java

# Verify Java installation into JAVA_HOME
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    echo
    echo "Found java executable in JAVA_HOME: $JAVA_HOME"

    _java="$JAVA_HOME/bin/java"

# Verify Java installation from linux repositories
elif type -p java;  then
    echo
    echo "Found installed java executable on the system"

    _java=java

else
    echo
    echo "Java is not installed . . . The required Java version is $required_java_version or higher"
    echo "Please install Java and try again. Script will be closed. "
    read DUMMY
    exit 15
fi

echo
echo "This script will apply all the AMPs in amps and amps_share to the alfresco.war and share.war files in $CATALINA_HOME/webapps"
echo "Press control-c to stop this script . . ."
echo "Press any other key to continue . . ."
read RESP
{
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar install $ALF_HOME/amps $CATALINA_HOME/webapps/alfresco.war -directory $*
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar list $CATALINA_HOME/webapps/alfresco.war
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar install $ALF_HOME/amps_share $CATALINA_HOME/webapps/share.war -directory $*
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar list $CATALINA_HOME/webapps/share.war
} ||
{
    echo
    echo "Error. Appling of the AMPs is failed. See error message above."
    echo
}
echo "About to clean out $ALF_HOME/tomcat/webapps/alfresco and share directories and temporary files..."
echo "Press control-c to stop this script . . ."
echo "Press any other key to continue . . ."
read DUMMY
rm -rf $CATALINA_HOME/webapps/alfresco
rm -rf $CATALINA_HOME/webapps/share
. $ALF_HOME/bin/clean_tomcat.sh
popd>/dev/null