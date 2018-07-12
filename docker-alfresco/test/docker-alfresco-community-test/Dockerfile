# Fetch image based on Tomcat 8.5.28 and Java 8
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-tomcat
FROM alfresco/alfresco-base-tomcat:8.5.28-java-8-oracle-centos-7

# Base ACS Repository Image includes transformation commands:
#   /usr/bin/alfresco-pdf-renderer      - alfresco-pdf-renderer
#   /usr/bin/convert                    - imagemagick
#   /opt/libreoffice5.4/program/soffice - LibreOffice

# alfresco-pdf-renderer uses the PDFium library from Google Inc. See the license at https://pdfium.googlesource.com/pdfium/+/master/LICENSE or in /pdfium.txt
# ImageMagick is from ImageMagick Studio LLC. See the license at http://www.imagemagick.org/script/license.php or in /ImageMagick-license.txt
# LibreOffice is from The Document Foundation. See the license at https://www.libreoffice.org/download/license/ or in /libreoffice.txt

ENV ALFRESCO_PDF_RENDERER_LIB_RPM_URL=https://nexus.alfresco.com/nexus/service/local/repositories/releases/content/org/alfresco/alfresco-pdf-renderer/1.1/alfresco-pdf-renderer-1.1-linux.tgz
ENV PDFIUM_LICENSE_FILE=https://github.com/Alfresco/acs-community-packaging/blob/master/distribution/src/main/resources/licenses/3rd-party/pdfium.txt

ENV LIBREOFFICE_RPM_URL=https://nexus.alfresco.com/nexus/service/local/repositories/thirdparty/content/org/libreoffice/libreoffice-dist/5.4.6/libreoffice-dist-5.4.6-linux.gz
ENV LIBREOFFICE_LICENSE_FILE=https://github.com/Alfresco/acs-community-packaging/blob/master/distribution/src/main/resources/licenses/3rd-party/libreoffice.txt

ENV IMAGEMAGICK_RPM_URL=https://nexus.alfresco.com/nexus/service/local/repositories/thirdparty/content/org/imagemagick/imagemagick-distribution/7.0.7-27/imagemagick-distribution-7.0.7-27-linux.rpm
ENV IMAGEMAGICK_LIB_RPM_URL=https://nexus.alfresco.com/nexus/service/local/repositories/thirdparty/content/org/imagemagick/imagemagick-distribution/7.0.7-27/imagemagick-distribution-7.0.7-27-libs-linux.rpm
ENV IMAGEMAGICK_LICENSE_FILE=https://github.com/Alfresco/acs-community-packaging/blob/master/distribution/src/main/resources/licenses/3rd-party/ImageMagick-license.txt

# Install transformer binaries
RUN yum install wget -y && \
    \
    wget $ALFRESCO_PDF_RENDERER_LIB_RPM_URL && \
    wget -P /. $PDFIUM_LICENSE_FILE && \
    tar xf alfresco-pdf-renderer-*-linux.tgz -C /usr/bin && \
    rm -f alfresco-pdf-renderer-*-linux.tgz && \
    \
    yum install -y cairo cups-libs libSM && \
    wget $LIBREOFFICE_RPM_URL && \
    wget -P /. $LIBREOFFICE_LICENSE_FILE && \
    tar xzf libreoffice-dist-*-linux.gz && \
    yum localinstall -y LibreOffice*/RPMS/*.rpm && \
    rm -rf libreoffice-dist-*-linux.gz LibreOffice_*_Linux_x86-64_rpm && \
    \
    wget $IMAGEMAGICK_RPM_URL && \
    wget $IMAGEMAGICK_LIB_RPM_URL && \
    wget -P /. $IMAGEMAGICK_LICENSE_FILE && \
    yum localinstall -y imagemagick-distribution-*-linux.rpm && \
    rm -f imagemagick-distribution-*-linux.rpm && \
    \
    yum clean all &&\
    \
    yum remove wget -y

# Create prerequisite to store tools and properties
RUN mkdir -p /usr/local/tomcat/shared/classes/alfresco/extension && \
    mkdir /usr/local/tomcat/alfresco-mmt
RUN touch /usr/local/tomcat/shared/classes/alfresco-global.properties

# You need to run `mvn clean install` in the root of this project to update the following dependencies
# Copy the WAR files to the appropriate location for your application server
# Copy the JDBC drivers for the database you are using to the lib/ directory.
# Copy the alfresco-mmt.jar
COPY target/war /usr/local/tomcat/webapps
COPY target/connector/* /usr/local/tomcat/lib/
COPY target/alfresco-mmt/* /usr/local/tomcat/alfresco-mmt/
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/server-root/* /usr/local/tomcat/webapps

# Change the value of the shared.loader= property to the following:
# shared.loader=${catalina.base}/shared/classes
RUN sed -i "s/shared.loader=/shared.loader=\${catalina.base}\/shared\/classes/" /usr/local/tomcat/conf/catalina.properties

# Add here configurations for alfresco-global.properties
RUN echo -e '\n\
alfresco-pdf-renderer.root=/usr/bin/\n\
alfresco-pdf-renderer.exe=${alfresco-pdf-renderer.root}/alfresco-pdf-renderer\n\
\n\
jodconverter.enabled=true\n\
jodconverter.portNumbers=8100\n\
jodconverter.officeHome=/opt/libreoffice5.4/\n\
\n\
img.root=/usr/lib64/ImageMagick-7.0.7\n\
img.coders=/usr/lib64/ImageMagick-7.0.7/modules-Q16HDRI/coders\n\
img.config=/usr/lib64/ImageMagick-7.0.7/config-Q16HDRI\n\
img.exe=/usr/bin/convert\n\
' >> /usr/local/tomcat/shared/classes/alfresco-global.properties

# Add debug for testing
# RUN echo -e '\n\
# log4j.logger.org.alfresco.repo.content.transform.TransformerDebug=debug\n\
# ' >> /usr/local/tomcat/shared/classes/alfresco/extension/custom-log4j.properties

RUN mkdir -p /usr/local/tomcat/amps

# Copy the amps from build context to the appropriate location for your application server
COPY target/amps /usr/local/tomcat/amps

# Install amps on alfresco.war
RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps /usr/local/tomcat/webapps/alfresco -directory -nobackup -force

# Docker CMD from parent image starts the server

# Unpack ROOT.war and make alfresco and ROOT read-only.
RUN mkdir /usr/local/tomcat/webapps/ROOT && cd /usr/local/tomcat/webapps/ROOT && jar -xvf /usr/local/tomcat/webapps/ROOT.war && rm -f /usr/local/tomcat/webapps/ROOT.war && chmod -R =r /usr/local/tomcat/webapps && \
# Add catalina.policy to ROOT.war and alfresco.war
# Grant all security permissions to alfresco webapp because of numerous permissions required in order to work properly.
# Grant only deployXmlPermission to ROOT webapp.
    sed -i -e "\$a\grant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/alfresco\/-\" \{\n\    permission\ java.security.AllPermission\;\n\};\ngrant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/ROOT\/-\" \{\n\    permission org.apache.catalina.security.DeployXmlPermission \"ROOT\";\n\};" /usr/local/tomcat/conf/catalina.policy

# To remote debug into this image add: EXPOSE 8000
# Changes are also required to the docker-compose/docker-compose.yml file.
# EXPOSE 8000
