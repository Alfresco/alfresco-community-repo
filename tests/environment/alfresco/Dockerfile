FROM alfresco/alfresco-content-repository-community:latest

ARG TOMCAT_DIR=/usr/local/tomcat
ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG USERNAME=alfresco
ARG USERID=33000

USER root

RUN echo -e '\n\
# every second \n\
activities.feed.generator.cronExpression=* * * ? * * \n\
activities.post.lookup.cronExpression=* * * ? * * \n\
' >> ${TOMCAT_DIR}/shared/classes/alfresco-global.properties

USER ${USERID}