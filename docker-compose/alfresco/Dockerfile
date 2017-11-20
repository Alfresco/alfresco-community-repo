FROM docker-internal.alfresco.com/full-content-services-community:latest

RUN echo -e '\n\
db.driver=org.postgresql.Driver\n\
db.username=alfresco\n\
db.password=alfresco\n\
db.url=jdbc:postgresql://postgres:5432/alfresco\n\
\n\
solr.host=solr6\n\
solr.port=8983\n\
solr.secureComms=none\n\
solr.base.url=/solr\n\
index.subsystem.name=solr6\n\
' >> /usr/local/tomcat/shared/classes/alfresco-global.properties

