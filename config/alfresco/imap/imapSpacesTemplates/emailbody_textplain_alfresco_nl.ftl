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

Contentmap:   ${contentFolderUrl}
Content-URL:      ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Download-URL:     ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
WebDAV-URL:       ${contextUrl}${document.webdavUrl}
