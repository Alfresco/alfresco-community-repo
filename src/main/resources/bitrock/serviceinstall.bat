@echo off
rem -- Check if argument is INSTALL or REMOVE

if not ""%1"" == ""INSTALL"" goto remove

if exist @@BITROCK_INSTALLDIR@@\mysql\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mysql\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\postgresql\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\postgresql\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\apache2\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\apache2\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\tomcat\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\tomcat\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\resin\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\resin\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\jboss\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\jboss\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\wildfly\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\wildfly\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\openoffice\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\openoffice\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\subversion\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\subversion\scripts\serviceinstall.bat INSTALL)
rem RUBY_APPLICATION_INSTALL
if exist @@BITROCK_INSTALLDIR@@\mongodb\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mongodb\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\lucene\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\lucene\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\third_application\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\third_application\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\nginx\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\nginx\scripts\serviceinstall.bat INSTALL)
if exist @@BITROCK_INSTALLDIR@@\php\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\php\scripts\serviceinstall.bat INSTALL)
goto end

:remove

if exist @@BITROCK_INSTALLDIR@@\third_application\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\third_application\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\lucene\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\lucene\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\mongodb\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mongodb\scripts\serviceinstall.bat)
rem RUBY_APPLICATION_REMOVE
if exist @@BITROCK_INSTALLDIR@@\subversion\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\subversion\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\openoffice\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\openoffice\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\jboss\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\jboss\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\wildfly\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\wildfly\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\resin\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\resin\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\tomcat\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\tomcat\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\apache2\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\apache2\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\postgresql\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\postgresql\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\mysql\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\mysql\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\php\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\php\scripts\serviceinstall.bat)
if exist @@BITROCK_INSTALLDIR@@\nginx\scripts\serviceinstall.bat (start /MIN /WAIT cmd /C @@BITROCK_INSTALLDIR@@\nginx\scripts\serviceinstall.bat)
:end
