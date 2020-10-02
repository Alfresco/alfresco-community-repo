# Fetch image based on Tomcat 8.5.28 and Java 8
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-tomcat
FROM alfresco/alfresco-base-tomcat:8.5.28-java-8-oracle-centos-7

# Create prerequisite to store tools and properties
RUN mkdir -p /usr/local/tomcat/shared/classes/alfresco/extension && \
    mkdir /licenses && \
    mkdir /usr/local/tomcat/alfresco-mmt && \
    touch /usr/local/tomcat/shared/classes/alfresco-global.properties

# You need to run `mvn clean install` in the root of this project to update the following dependencies
# Copy the WAR files to the appropriate location for your application server
# Copy the JDBC drivers for the database you are using to the lib/ directory.
# Copy the alfresco-mmt.jar
# Copy Licenses to the root of the Docker image
COPY target/war /usr/local/tomcat/webapps
COPY target/connector/* /usr/local/tomcat/lib/
COPY target/alfresco-mmt/* /usr/local/tomcat/alfresco-mmt/
COPY target/dependency/licenses/ /licenses/

# Change the value of the shared.loader= property to the following:
# shared.loader=${catalina.base}/shared/classes
RUN sed -i "s/shared.loader=/shared.loader=\${catalina.base}\/shared\/classes/" /usr/local/tomcat/conf/catalina.properties

# Add debug for testing
# RUN echo -e '\n\
# log4j.logger.org.alfresco.repo.content.transform.TransformerDebug=debug\n\
# ' >> /usr/local/tomcat/shared/classes/alfresco/extension/custom-log4j.properties

RUN mkdir -p /usr/local/tomcat/amps

# Copy the amps from build context to the appropriate location for your application server
COPY target/amps /usr/local/tomcat/amps

# Install amps on alfresco.war
RUN chmod 664 /usr/local/tomcat/alfresco-mmt/alfresco-mmt-*.jar && \
    java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps /usr/local/tomcat/webapps/alfresco -directory -nobackup -force

# Docker CMD from parent image starts the server

# Make webapps folder read-only.
RUN chmod -R =r /usr/local/tomcat/webapps && \
# Add catalina.policy to ROOT.war and alfresco.war
# Grant all security permissions to alfresco webapp because of numerous permissions required in order to work properly.
# Grant only deployXmlPermission to ROOT webapp.
    sed -i -e "\$a\grant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/alfresco\/-\" \{\n\    permission\ java.security.AllPermission\;\n\};\ngrant\ codeBase\ \"file:\$\{catalina.base\}\/webapps\/ROOT\/-\" \{\n\    permission org.apache.catalina.security.DeployXmlPermission \"ROOT\";\n\};" /usr/local/tomcat/conf/catalina.policy

# To remote debug into this image add: EXPOSE 8000
# Changes are also required to the docker-compose/docker-compose.yml file.
# EXPOSE 8000
