FROM quay.io/alfresco/share:5.2.X

RUN mkdir -p /usr/local/tomcat/amps_share

COPY target/alfresco-mmt/* /usr/local/tomcat/alfresco-mmt/
COPY target/amps_share /usr/local/tomcat/amps_share

RUN sed -i "s/localhost:8080/alfresco:8082/" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps_share /usr/local/tomcat/webapps/share -directory -nobackup -force