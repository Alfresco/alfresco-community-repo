ARG TAG
FROM alfresco/alfresco-search-services:${TAG}

ENV DIST_DIR /opt/alfresco-search-services

COPY extra-config.sh .

# add config script in 3rd line
RUN sed -i "3isource $PWD/extra-config.sh" \
    /opt/alfresco-search-services/solr/bin/search_config_setup.sh