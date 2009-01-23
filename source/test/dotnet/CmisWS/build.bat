@echo off

set SDK_LOCATION=c:\WINDOWS\Microsoft.NET\Framework
set COMPILER=%SDK_LOCATION%\v3.5\MSBuild.exe

if not exist %COMPILER% goto reportErrorAndExit

%COMPILER% WcfCmisWSTests.csproj

goto end

:reportErrorAndExit
echo  --- CRITICAL ENVIRONMENT ERROR: .NET v3.5 compiler ("%COMPILER%") was not found at "%SDK_LOCATION%" location! Please, install .NET v3.5 SDK or reconfigure SDK_LOCATION variable
:end

pause