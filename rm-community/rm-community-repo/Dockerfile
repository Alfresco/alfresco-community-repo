FROM alfresco/alfresco-content-repository-community:6.0.7-ga

COPY target/alfresco-rm-community-repo-*.amp /usr/local/tomcat/amps/

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps/alfresco-rm-community-repo-*.amp /usr/local/tomcat/webapps/alfresco -nobackup
