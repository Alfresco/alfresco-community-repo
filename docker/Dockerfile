FROM quay.io/alfresco/alfresco-base-repository:0.1.1

RUN mkdir -p /usr/local/tomcat/shared/classes && \
    mkdir /usr/local/tomcat/alfresco-mmt

RUN touch /usr/local/tomcat/shared/classes/alfresco-global.properties

COPY target/war /usr/local/tomcat/webapps
COPY target/connector/* /usr/local/tomcat/lib/
COPY target/alfresco-mmt/* /usr/local/tomcat/alfresco-mmt/
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/server-root/* /usr/local/tomcat/webapps

RUN sed -i "s/shared.loader=/shared.loader=\${catalina.base}\/shared\/classes/" /usr/local/tomcat/conf/catalina.properties

RUN echo -e '\n\
alfresco-pdf-renderer.exe=/usr/bin/alfresco-pdf-renderer\n\
ooo.enabled=false\n\
jodconverter.officeHome=/opt/libreoffice5.4/\n\
jodconverter.enabled=true\n\
img.root=/usr/lib64/ImageMagick-7.0.7\n\
img.coders=/usr/lib64/ImageMagick-7.0.7/modules-Q16HDRI/coders\n\
img.config=/usr/lib64/ImageMagick-7.0.7/config-Q16HDRI\n\
img.exe=/usr/bin/convert\n\
' >> /usr/local/tomcat/shared/classes/alfresco-global.properties