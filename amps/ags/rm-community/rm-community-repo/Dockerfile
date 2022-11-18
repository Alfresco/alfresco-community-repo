# BUILD STAGE AGS
FROM debian:11-slim AS AGSBUILDER

RUN export DEBIAN_FRONTEND=noninteractive; \
    apt-get update -qqy && apt-get -yqq install unzip && \
    mkdir -p /build/gs-api-explorer

### Copy the AGS war from the local context
COPY target/gs-api-explorer-*.war /build

RUN unzip -q /build/gs-api-explorer-*.war -d /build/gs-api-explorer && \
    chmod -R g-w,o= /build

# ACTUAL IMAGE
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

### Copy gs-api-explorer
COPY --chown=root:Alfresco --from=AGSBUILDER /build/gs-api-explorer /usr/local/tomcat/webapps/gs-api-explorer

# All files in the tomcat folder must be owned by root user and Alfresco group as mentioned in the parent Dockerfile
RUN chgrp -R Alfresco /usr/local/tomcat && \
    find /usr/local/tomcat/webapps -type d -exec chmod 0750 {} \; && \
    find /usr/local/tomcat/webapps -type f -exec chmod 0640 {} \;

# Switching back to alfresco user after having added amps files to run the container as non-root
USER alfresco
