@echo off

set CLASSPATH=cmis-test-client.jar
for %%i in (./lib/*.jar) do call set CLASSPATH=./lib/%%~i;%%CLASSPATH%%

java org.alfresco.cmis.ws.example.CmisSampleClient %1 %2 %3
