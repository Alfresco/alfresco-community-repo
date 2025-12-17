# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-tomcat
FROM alfresco/alfresco-base-tomcat:tomcat11-jre21-rockylinux9@sha256:9e76ad5868b5238fb56b825256ceb488a887bb59cb3b95bec2d9823af00a28b6
# Set default docker_context.
ARG resource_path=target

# Set default user information
ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG IMAGEUSERNAME=alfresco
ARG USERID=33000

# Set default environment args
ARG TOMCAT_DIR=/usr/local/tomcat

# Needed for installation but make sure another USER directive is added after
# this with a non-root user
USER root

# Create prerequisite to store tools and properties
RUN mkdir -p ${TOMCAT_DIR}/shared/classes/alfresco/extension/mimetypes && \
    mkdir -p ${TOMCAT_DIR}/shared/classes/alfresco/extension/transform/renditions && \
    mkdir -p ${TOMCAT_DIR}/shared/classes/alfresco/extension/transform/pipelines && \
    mkdir /licenses && \
    mkdir -p ${TOMCAT_DIR}/shared/classes/alfresco/extension/keystore && \
    mkdir ${TOMCAT_DIR}/alfresco-mmt && \
    touch ${TOMCAT_DIR}/shared/classes/alfresco-global.properties

# You need to run `mvn clean install` in the root of this project to update the following dependencies
# Copy the WAR files to the appropriate location for your application server
# Copy the JDBC drivers for the database you are using to the lib/ directory.
# Copy the alfresco-mmt.jar
# Copy Licenses to the root of the Docker image
# Copy default keystore
COPY ${resource_path}/war ${TOMCAT_DIR}/webapps
COPY ${resource_path}/connector/* ${TOMCAT_DIR}/lib/
COPY ${resource_path}/alfresco-mmt/* ${TOMCAT_DIR}/alfresco-mmt/
COPY ${resource_path}/dependency/licenses/ /licenses/
COPY ${resource_path}/dependency/keystore/metadata-keystore/keystore ${TOMCAT_DIR}/shared/classes/alfresco/extension/keystore

# Change the value of the shared.loader= property to the following:
# shared.loader=${catalina.base}/shared/classes
RUN sed -i "s/shared.loader=/shared.loader=\${catalina.base}\/shared\/classes/" ${TOMCAT_DIR}/conf/catalina.properties

RUN mkdir -p ${TOMCAT_DIR}/amps

# Copy the amps from build context to the appropriate location for your application server
COPY ${resource_path}/amps ${TOMCAT_DIR}/amps

# Install amps on alfresco.war
RUN java -jar ${TOMCAT_DIR}/alfresco-mmt/alfresco-mmt*.jar install \
              ${TOMCAT_DIR}/amps \
              ${TOMCAT_DIR}/webapps/alfresco -directory -nobackup

# Move the log file
RUN sed -i -e "s_appender.rolling.fileName\=alfresco.log_appender.rolling.fileName\=${TOMCAT_DIR}/logs\/alfresco.log_" \
        ${TOMCAT_DIR}/webapps/alfresco/WEB-INF/classes/log4j2.properties && \
    sed -i -e "s_appender.rolling.filePattern=alfresco.log.%d{yyyy-MM-dd}_appender.rolling.filePattern\=${TOMCAT_DIR}/logs\/alfresco.log.%d{yyyy-MM-dd}_" \
        ${TOMCAT_DIR}/webapps/alfresco/WEB-INF/classes/log4j2.properties && \
# Add catalina.policy to ROOT.war and alfresco.war
# Grant all security permissions to alfresco webapp because of numerous permissions required in order to work properly.
# Grant only deployXmlPermission to ROOT webapp.
    sed -i -e "\$a\grant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/alfresco\/-\" \{\n\    permission\ java.security.AllPermission\;\n\};\ngrant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/_vti_bin\/-\" \{\n\    permission\ java.security.AllPermission\;\n\};\ngrant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/ROOT\/-\" \{\n\    permission org.apache.catalina.security.DeployXmlPermission \"ROOT\";\n\};" ${TOMCAT_DIR}/conf/catalina.policy

# fontconfig is required by Activiti worflow diagram generator
RUN yum install -y fontconfig-2.14.0-2.el9_1 && \
    yum clean all

# The standard configuration is to have all Tomcat files owned by root with group GROUPNAME and whilst owner has read/write privileges,
# group only has restricted permissions and world has no permissions.
RUN mkdir -p ${TOMCAT_DIR}/conf/Catalina/localhost && \
    mkdir -p ${TOMCAT_DIR}/alf_data && \
    groupadd -g ${GROUPID} ${GROUPNAME} && \
    useradd -u ${USERID} -G ${GROUPNAME} ${IMAGEUSERNAME} && \
    chgrp -R ${GROUPNAME} ${TOMCAT_DIR} && \
    chmod g+rx ${TOMCAT_DIR}/conf && \
    chmod -R g+r ${TOMCAT_DIR}/conf && \
    find ${TOMCAT_DIR}/webapps -type d -exec chmod 0750 {} \; && \
    find ${TOMCAT_DIR}/webapps -type f -exec chmod 0640 {} \; && \
    chmod -R g+r ${TOMCAT_DIR}/webapps && \
    chmod g+r ${TOMCAT_DIR}/conf/Catalina && \
    chmod g+rwx ${TOMCAT_DIR}/alf_data && \
    chmod g+rwx ${TOMCAT_DIR}/logs && \
    chmod o-w ${TOMCAT_DIR}/logs && \
    chmod g+rwx ${TOMCAT_DIR}/temp && \
    chmod g+rwx ${TOMCAT_DIR}/work && \
    chmod o-w ${TOMCAT_DIR}/work && \
    chmod 664 ${TOMCAT_DIR}/alfresco-mmt/alfresco-mmt-*.jar && \
    find /licenses -type d -exec chmod 0755 {} \; && \
    find /licenses -type f -exec chmod 0644 {} \;

EXPOSE 10001

# For remote debug
EXPOSE 8000

USER ${IMAGEUSERNAME}
