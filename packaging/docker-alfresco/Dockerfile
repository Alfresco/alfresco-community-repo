# Fetch image based on Tomcat 8.5.34 and Java 11
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-tomcat
FROM alfresco/alfresco-base-tomcat:8.5.34-java-11-openjdk-centos-7

# Set default docker_context.
ARG resource_path=target

# Create prerequisite to store tools and properties
RUN mkdir -p /usr/local/tomcat/shared/classes/alfresco/extension && \
    mkdir /licenses && \
    mkdir /usr/local/tomcat/alfresco-mmt
RUN touch /usr/local/tomcat/shared/classes/alfresco-global.properties

# You need to run `mvn clean install` in the root of this project to update the following dependencies
# Copy the WAR files to the appropriate location for your application server
# Copy the JDBC drivers for the database you are using to the lib/ directory.
# Copy the alfresco-mmt.jar
# Copy Licenses to the root of the Docker image
COPY ${resource_path}/war /usr/local/tomcat/webapps
COPY ${resource_path}/connector/* /usr/local/tomcat/lib/
COPY ${resource_path}/alfresco-mmt/* /usr/local/tomcat/alfresco-mmt/
COPY ${resource_path}/dependency/licenses/ /licenses/

# Change the value of the shared.loader= property to the following:
# shared.loader=${catalina.base}/shared/classes
RUN sed -i "s/shared.loader=/shared.loader=\${catalina.base}\/shared\/classes/" /usr/local/tomcat/conf/catalina.properties

# Add debug for testing
# RUN echo -e '\n\
# log4j.logger.org.alfresco.repo.content.transform.TransformerDebug=debug\n\
# ' >> /usr/local/tomcat/shared/classes/alfresco/extension/custom-log4j.properties

RUN mkdir -p /usr/local/tomcat/amps

# Copy the amps from build context to the appropriate location for your application server
COPY ${resource_path}/amps /usr/local/tomcat/amps

# Install amps on alfresco.war
RUN chmod 664 /usr/local/tomcat/alfresco-mmt/alfresco-mmt-*.jar && \
    java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps /usr/local/tomcat/webapps/alfresco -directory -nobackup -force

# Make webapps folder read-only.
RUN chmod -R =r /usr/local/tomcat/webapps && \
# Add catalina.policy to ROOT.war and alfresco.war
# Grant all security permissions to alfresco webapp because of numerous permissions required in order to work properly.
# Grant only deployXmlPermission to ROOT webapp.
    sed -i -e "\$a\grant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/alfresco\/-\" \{\n\    permission\ java.security.AllPermission\;\n\};\ngrant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/ROOT\/-\" \{\n\    permission org.apache.catalina.security.DeployXmlPermission \"ROOT\";\n\};" /usr/local/tomcat/conf/catalina.policy

# fontconfig is required by Activiti worflow diagram generator
# installing pinned dependencies as well
RUN yum install -y fontconfig-2.13.0-4.3.el7 \
                   dejavu-fonts-common-2.33-6.el7 \
                   fontpackages-filesystem-1.44-8.el7 \
                   freetype-2.8-12.el7 \
                   libpng-1.5.13-7.el7_2 \
                   dejavu-sans-fonts-2.33-6.el7 && \
    yum clean all

RUN find /licenses -type d -exec chmod 0775 {} \; && \
    find /licenses -type f -exec chmod 0664 {} \;

EXPOSE 10001

# For remote debug
EXPOSE 8000
