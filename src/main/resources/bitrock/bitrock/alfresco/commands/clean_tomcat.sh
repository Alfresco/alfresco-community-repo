#!/bin/sh
# ---------------------------------
# Script to clean Tomcat temp files
# ---------------------------------
echo "Cleaning temporary Alfresco files from Tomcat..."
rm -rf @@BITROCK_INSTALLDIR@@/@@BITROCK_TOMCAT_DIRNAME@@/temp/Alfresco
rm -rf @@BITROCK_INSTALLDIR@@/@@BITROCK_TOMCAT_DIRNAME@@/work/Catalina/localhost/alfresco
rm -rf @@BITROCK_INSTALLDIR@@/@@BITROCK_TOMCAT_DIRNAME@@/work/Catalina/localhost/share
rm -rf @@BITROCK_INSTALLDIR@@/@@BITROCK_TOMCAT_DIRNAME@@/work/Catalina/localhost/awe
rm -rf @@BITROCK_INSTALLDIR@@/@@BITROCK_TOMCAT_DIRNAME@@/work/Catalina/localhost/wcmqs