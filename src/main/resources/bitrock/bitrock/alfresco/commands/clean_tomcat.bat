rem @echo off
rem ---------------------------------
rem Script to clean Tomcat temp files
rem ---------------------------------

set ALF_HOME=@@BITROCK_INSTALLDIR@@
set CATALINA_HOME=%ALF_HOME%\@@BITROCK_TOMCAT_DIRNAME@@

echo Cleaning temporary Alfresco files from Tomcat...
rmdir /S /Q "%CATALINA_HOME%\temp\Alfresco" "%CATALINA_HOME%\work\Catalina\localhost\alfresco"
rmdir /S /Q "%CATALINA_HOME%\work\Catalina\localhost\share"
rmdir /S /Q "%CATALINA_HOME%\work\Catalina\localhost\awe"
rmdir /S /Q "%CATALINA_HOME%\work\Catalina\localhost\wcmqs"