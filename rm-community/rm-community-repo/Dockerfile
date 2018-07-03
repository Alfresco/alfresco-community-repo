FROM alfresco/alfresco-content-repository-community:6.0.7-ga

COPY target/alfresco-rm-community-repo-*.amp /usr/local/tomcat/amps/

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps/alfresco-rm-community-repo-*.amp /usr/local/tomcat/webapps/alfresco -nobackup

### Copy gs-api-explorer war into webapps folder
COPY target/gs-api-explorer-*.war /usr/local/tomcat/webapps/

### Unpack gs-api-explorer.war
RUN mkdir /usr/local/tomcat/webapps/gs-api-explorer && cd /usr/local/tomcat/webapps/gs-api-explorer && \
    jar -xvf /usr/local/tomcat/webapps/gs-api-explorer-*.war && rm -f /usr/local/tomcat/webapps/gs-api-explorer-*.war
