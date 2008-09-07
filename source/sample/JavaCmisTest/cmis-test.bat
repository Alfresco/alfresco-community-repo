@echo off

set CLASSPATH=cmis-test.jar
for %%i in (./lib/*.jar) do call set CLASSPATH=./lib/%%~i;%%CLASSPATH%%

java org.alfresco.cmis.ws.example.SimpleExecutableCmisServicesUtilizer %1 %2 %3
