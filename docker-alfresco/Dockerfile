FROM docker-internal.alfresco.com/content-services-community:latest

RUN mkdir -p /usr/local/tomcat/amps

COPY target/amps /usr/local/tomcat/amps

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps /usr/local/tomcat/webapps/alfresco -directory -nobackup -force