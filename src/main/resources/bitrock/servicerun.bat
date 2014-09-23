@echo off
rem START or STOP Services
rem ----------------------------------
rem Check if argument is STOP or START

if not ""%1"" == ""START"" goto stop

if exist @@BITROCK_INSTALLDIR@@\hypersonic\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\server\hsql-sample-database\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\ingres\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\ingres\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\mysql\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mysql\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\postgresql\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\postgresql\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\apache2\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\apache2\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\openoffice\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\openoffice\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\tomcat\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\tomcat\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\resin\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\resin\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\jboss\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\jboss\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\wildfly\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\wildfly\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\jetty\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\jetty\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\subversion\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\subversion\scripts\servicerun.bat START)
rem RUBY_APPLICATION_START
if exist @@BITROCK_INSTALLDIR@@\lucene\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\lucene\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\mongodb\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mongodb\scripts\servicerun.bat START)
if exist @@BITROCK_INSTALLDIR@@\third_application\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\third_application\scripts\servicerun.bat START)
goto end

:stop
echo "Stopping services ..."
if exist @@BITROCK_INSTALLDIR@@\third_application\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\third_application\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\lucene\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\lucene\scripts\servicerun.bat STOP)
rem RUBY_APPLICATION_STOP
if exist @@BITROCK_INSTALLDIR@@\subversion\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\subversion\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\jetty\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\jetty\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\hypersonic\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\server\hsql-sample-database\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\jboss\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\jboss\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\wildfly\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\wildfly\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\resin\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\resin\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\tomcat\scripts\servicerun.bat (start /MIN /WAIT cmd /C /WAIT @@BITROCK_INSTALLDIR@@\tomcat\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\openoffice\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\openoffice\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\apache2\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\apache2\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\ingres\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\ingres\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\mysql\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mysql\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\mongodb\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mongodb\scripts\servicerun.bat STOP)
if exist @@BITROCK_INSTALLDIR@@\postgresql\scripts\servicerun.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\postgresql\scripts\servicerun.bat STOP)

:end
