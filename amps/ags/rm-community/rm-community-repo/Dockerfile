### Apply AGS community repo AMP to ACS image
FROM alfresco/alfresco-community-repo-base:${image.tag}

# Alfresco user does not have permissions to modify webapps or configuration. Switch to root.
# The access will be fixed after all operations are done.
USER root

COPY target/alfresco-governance-services-community-repo-*.amp /usr/local/tomcat/amps/
COPY target/alfresco-share-services-*.amp /usr/local/tomcat/amps/

# Install amps on alfresco.war
RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt*.jar install \
              /usr/local/tomcat/amps \
              /usr/local/tomcat/webapps/alfresco -directory -nobackup

### Copy gs-api-explorer war into webapps folder
COPY target/gs-api-explorer-*.war /usr/local/tomcat/webapps/

### Unpack gs-api-explorer.war
RUN mkdir /usr/local/tomcat/webapps/gs-api-explorer && cd /usr/local/tomcat/webapps/gs-api-explorer && \
    jar -xvf /usr/local/tomcat/webapps/gs-api-explorer-*.war && rm -f /usr/local/tomcat/webapps/gs-api-explorer-*.war

# All files in the tomcat folder must be owned by root user and Alfresco group as mentioned in the parent Dockerfile
RUN chgrp -R Alfresco /usr/local/tomcat && \
    find /usr/local/tomcat/webapps -type d -exec chmod 0750 {} \; && \
    find /usr/local/tomcat/webapps -type f -exec chmod 0640 {} \; && \
    chmod -R g+r /usr/local/tomcat/webapps

# Switching back to alfresco user after having added amps files to run the container as non-root
USER alfresco
