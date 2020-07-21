#!/bin/sh
# ---------------------------------
# Script to clean Tomcat temp files
# ---------------------------------
echo "Cleaning temporary Alfresco files from Tomcat..."
rm -rf tomcat/temp/Alfresco tomcat/work/Catalina/localhost/alfresco
rm -rf tomcat/work/Catalina/localhost/share
rm -rf tomcat/work/Catalina/localhost/awe
rm -rf tomcat/work/Catalina/localhost/wcmqs