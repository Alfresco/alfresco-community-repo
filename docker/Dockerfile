FROM quay.io/alfresco/alfresco-base-tomcat:0.1.0

RUN mkdir -p /usr/local/tomcat/shared/classes

RUN touch /usr/local/tomcat/shared/classes/alfresco-global.properties

COPY target/war /usr/local/tomcat/webapps
COPY target/connector/* /usr/local/tomcat/lib/

RUN sed -i "s/shared.loader=/shared.loader=\${catalina.base}\/shared\/classes/" /usr/local/tomcat/conf/catalina.properties

RUN echo -e '\n\
ooo.enabled=false\n\
jodconverter.officeHome=/opt/libreoffice5.4/\n\
jodconverter.enabled=true\n\
' >> /usr/local/tomcat/shared/classes/alfresco-global.properties