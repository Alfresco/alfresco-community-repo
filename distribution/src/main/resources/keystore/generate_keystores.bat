@rem Please edit the variables below to suit your installation
@rem Note: for an installation created by the Alfresco installer, you only need to edit ALFRESCO_HOME

@rem Alfresco installation directory
set ALFRESCO_HOME=C:\Alfresco-5.2
@rem The directory containing the alfresco keystores, as referenced by keystoreFile and truststoreFile attributes in tomcat\conf\server.xml
set ALFRESCO_KEYSTORE_HOME=%ALFRESCO_HOME%\alf_data\keystore
@rem Java installation directory
set JAVA_HOME=%ALFRESCO_HOME%\java
@rem Location in which new keystore files will be generated
set CERTIFICATE_HOME=%USERPROFILE%
@rem The repository server certificate subject name, as specified in tomcat\conf\tomcat-users.xml with roles="repository"
set REPO_CERT_DNAME=CN=Alfresco Repository, OU=Unknown, O=Alfresco Software Ltd., L=Maidenhead, ST=UK, C=GB
@rem The SOLR client certificate subject name, as specified in tomcat\conf\tomcat-users.xml with roles="repoclient"
set SOLR_CLIENT_CERT_DNAME=CN=Alfresco Repository Client, OU=Unknown, O=Alfresco Software Ltd., L=Maidenhead, ST=UK, C=GB
@rem The number of days before the certificate expires
set CERTIFICATE_VALIDITY=36525

@rem Ensure certificate output dir exists
@if not exist "%CERTIFICATE_HOME%" mkdir "%CERTIFICATE_HOME%"

@rem Remove old output files (note they are backed up elsewhere)
@if exist "%CERTIFICATE_HOME%\ssl.keystore" del "%CERTIFICATE_HOME%\ssl.keystore"
@if exist "%CERTIFICATE_HOME%\ssl.truststore" del "%CERTIFICATE_HOME%\ssl.truststore"
@if exist "%CERTIFICATE_HOME%\browser.p12" del "%CERTIFICATE_HOME%\browser.p12"
@if exist "%CERTIFICATE_HOME%\ssl.repo.client.keystore" del "%CERTIFICATE_HOME%\ssl.repo.client.keystore"
@if exist "%CERTIFICATE_HOME%\ssl.repo.client.truststore" del "%CERTIFICATE_HOME%\ssl.repo.client.truststore"

@rem Generate new self-signed certificates for the repository and solr
"%JAVA_HOME%\bin\keytool" -genkeypair -keyalg RSA -dname "%REPO_CERT_DNAME%" -validity %CERTIFICATE_VALIDITY% -alias ssl.repo -keypass kT9X6oe68t -keystore "%CERTIFICATE_HOME%\ssl.keystore" -storetype JCEKS -storepass kT9X6oe68t
"%JAVA_HOME%\bin\keytool" -exportcert -alias ssl.repo -file "%CERTIFICATE_HOME%\ssl.repo.crt" -keystore "%CERTIFICATE_HOME%\ssl.keystore" -storetype JCEKS -storepass kT9X6oe68t
"%JAVA_HOME%\bin\keytool" -genkeypair -keyalg RSA -dname "%SOLR_CLIENT_CERT_DNAME%" -validity %CERTIFICATE_VALIDITY% -alias ssl.repo.client -keypass kT9X6oe68t -keystore "%CERTIFICATE_HOME%\ssl.repo.client.keystore" -storetype JCEKS -storepass kT9X6oe68t
"%JAVA_HOME%\bin\keytool" -exportcert -alias ssl.repo.client -file "%CERTIFICATE_HOME%\ssl.repo.client.crt" -keystore "%CERTIFICATE_HOME%\ssl.repo.client.keystore" -storetype JCEKS -storepass kT9X6oe68t

@rem Create trust relationship between repository and solr
"%JAVA_HOME%\bin\keytool" -importcert -noprompt -alias ssl.repo.client -file "%CERTIFICATE_HOME%\ssl.repo.client.crt" -keystore "%CERTIFICATE_HOME%\ssl.truststore" -storetype JCEKS -storepass kT9X6oe68t
@rem Create trust relationship between repository and itself - used for searches
"%JAVA_HOME%\bin\keytool" -importcert -noprompt -alias ssl.repo -file "%CERTIFICATE_HOME%\ssl.repo.crt" -keystore "%CERTIFICATE_HOME%\ssl.truststore" -storetype JCEKS -storepass kT9X6oe68t
@rem Create trust relationship between solr and repository
"%JAVA_HOME%\bin\keytool" -importcert -noprompt -alias ssl.repo -file "%CERTIFICATE_HOME%\ssl.repo.crt" -keystore "%CERTIFICATE_HOME%\ssl.repo.client.truststore" -storetype JCEKS -storepass kT9X6oe68t
@rem Export repository keystore to pkcs12 format for browser compatibility
"%JAVA_HOME%\bin\keytool" -importkeystore -srckeystore "%CERTIFICATE_HOME%\ssl.keystore" -srcstorepass kT9X6oe68t -srcstoretype JCEKS -srcalias ssl.repo -srckeypass kT9X6oe68t -destkeystore "%CERTIFICATE_HOME%\browser.p12" -deststoretype pkcs12 -deststorepass alfresco -destalias ssl.repo -destkeypass alfresco

@rem Ensure keystore dir actually exists
@if not exist "%ALFRESCO_KEYSTORE_HOME%" mkdir "%ALFRESCO_KEYSTORE_HOME%"

@rem Install the new files
copy /Y "%CERTIFICATE_HOME%\ssl.keystore" "%ALFRESCO_KEYSTORE_HOME%\ssl.keystore"
copy /Y "%CERTIFICATE_HOME%\ssl.truststore" "%ALFRESCO_KEYSTORE_HOME%\ssl.truststore"
copy /Y "%CERTIFICATE_HOME%\browser.p12" "%ALFRESCO_KEYSTORE_HOME%\browser.p12"

@echo ****************************************
@echo You must copy the following files to the correct location.
@echo %CERTIFICATE_HOME%\ssl.repo.client.keystore
@echo %CERTIFICATE_HOME%\ssl.repo.client.truststore
@echo eg. for Solr 4 the location is SOLR_HOME\workspace-SpacesStore\conf and SOLR_HOME\archive-SpacesStore\conf  
@echo Please ensure that you set dir.keystore=%ALFRESCO_KEYSTORE_HOME% in alfresco-global.properties
@echo ****************************************