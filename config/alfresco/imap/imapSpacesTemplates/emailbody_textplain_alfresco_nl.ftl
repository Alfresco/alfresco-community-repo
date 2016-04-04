------------------------------------------------------------------------------
Documentnaam:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titel:   ${document.properties.title?html}
   <#else>
Titel:         GEEN
   </#if>
   <#if document.properties.description?exists>
Beschrijving:   ${document.properties.description?html}
   <#else>
Beschrijving:  GEEN
   </#if>
Maker:      ${document.properties.creator?html}
Gemaakt:    ${document.properties.created?datetime}
Wijziger:   ${document.properties.modifier?html}
Gewijzigd:  ${document.properties.modified?datetime}
Grootte:    ${document.size / 1024} kB


CONTENTKOPPELINGEN

Contentmap:    ${contentFolderUrl?html}
Content-URL:   ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
Download-URL:  ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
WebDAV-URL:    ${contextUrl}${document.webdavUrl?html}
