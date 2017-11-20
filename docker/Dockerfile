FROM docker-internal.alfresco.com/content-services-community:latest

RUN mkdir -p /usr/local/tomcat/amps && \
    mkdir -p /usr/local/tomcat/amps_share

COPY target/amps /usr/local/tomcat/amps
COPY target/amps_share /usr/local/tomcat/amps_share
COPY target/war /usr/local/tomcat/webapps

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
        /usr/local/tomcat/amps /usr/local/tomcat/webapps/alfresco -directory -nobackup -force && \
    java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
        /usr/local/tomcat/amps_share /usr/local/tomcat/webapps/share -directory -nobackup -force