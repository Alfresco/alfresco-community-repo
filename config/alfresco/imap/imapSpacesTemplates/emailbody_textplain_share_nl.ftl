------------------------------------------------------------------------------
Documentnaam:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titel:   ${document.properties.title}
   <#else>
Titel:         GEEN
   </#if>
   <#if document.properties.description?exists>
Beschrijving:   ${document.properties.description}
   <#else>
Beschrijving:   GEEN
   </#if>
Maker:   ${document.properties.creator}
Gemaakt op:   ${document.properties.created?datetime}
Gewijzigd door:  ${document.properties.modifier}
Gewijzigd:  ${document.properties.modified?datetime}
Grootte:      ${document.size / 1024} Kb


CONTENTKOPPELINGEN

Contentmap:   ${shareContextUrl}/page/site/${parentPathFromSites}
Content-URL:      ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Download-URL:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
