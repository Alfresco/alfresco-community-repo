

FROM alfresco/alfresco-content-repository-community:latest

ARG TOMCAT_DIR=/usr/local/tomcat

COPY target/alfresco-rm-community-repo-*.amp $TOMCAT_DIR/amps/

RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps/alfresco-rm-community-repo-*.amp $TOMCAT_DIR/webapps/alfresco -nobackup
