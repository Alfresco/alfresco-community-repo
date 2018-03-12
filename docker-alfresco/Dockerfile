FROM quay.io/alfresco/alfresco-base-tomcat:0.1.0

# Base ACS Repository Image includes transformation commands:
#   /usr/bin/alfresco-pdf-renderer      - alfresco-pdf-renderer
#   /usr/bin/convert                    - imagemagick
#   /opt/libreoffice5.4/program/soffice - LibreOffice

ENV ALFRESCO_PDF_RENDERER_LIB_RPM_URL=https://nexus.alfresco.com/nexus/service/local/repositories/releases/content/org/alfresco/alfresco-pdf-renderer/1.0/alfresco-pdf-renderer-1.0-linux.tgz
ENV LIBREOFFICE_RPM_URL=http://download.documentfoundation.org/libreoffice/stable/5.4.5/rpm/x86_64/LibreOffice_5.4.5_Linux_x86-64_rpm.tar.gz
ENV IMAGEMAGICK_RPM_URL=https://www.imagemagick.org/download/linux/CentOS/x86_64/ImageMagick-7.0.7-26.x86_64.rpm
ENV IMAGEMAGICK_LIB_RPM_URL=https://www.imagemagick.org/download/linux/CentOS/x86_64/ImageMagick-libs-7.0.7-26.x86_64.rpm

RUN wget $ALFRESCO_PDF_RENDERER_LIB_RPM_URL && \
    tar xf alfresco-pdf-renderer-*-linux.tgz -C /usr/bin && \
	rm -f alfresco-pdf-renderer-*-linux.tgz && \
	\
    yum install -y cairo cups-libs libSM && \
    wget $LIBREOFFICE_RPM_URL && \
    tar xzf LibreOffice_*_Linux_x86-64_rpm.tar.gz && \
    yum localinstall -y LibreOffice*/RPMS/*.rpm && \
    rm -rf LibreOffice_*_Linux_x86-64_rpm.tar.gz LibreOffice_*_Linux_x86-64_rpm && \
	\
    wget $IMAGEMAGICK_RPM_URL && \
    wget $IMAGEMAGICK_LIB_RPM_URL && \
    yum localinstall -y ImageMagick-*.x86_64.rpm && \
    rm -f ImageMagick-*.x86_64.rpm && \
	\
    yum clean all

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

RUN mkdir -p /usr/local/tomcat/amps

COPY target/amps /usr/local/tomcat/amps

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps /usr/local/tomcat/webapps/alfresco -directory -nobackup -force
