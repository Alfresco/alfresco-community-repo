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

Contentmap:   ${contextUrl}/navigate/browse${document.parent.webdavUrl}
Content-URL:      ${contextUrl}${document.url}
Download-URL:     ${contextUrl}${document.downloadUrl}
WebDAV-URL:       ${contextUrl}${document.webdavUrl}
