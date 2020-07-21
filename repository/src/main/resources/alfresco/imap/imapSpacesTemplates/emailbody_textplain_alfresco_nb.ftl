------------------------------------------------------------------------------
Dokumentnavn:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Tittel:   ${document.properties.title?html}
   <#else>
Tittel:       INGEN
   </#if>
   <#if document.properties.description?exists>
Beskrivelse:   ${document.properties.description?html}
   <#else>
Beskrivelse:  INGEN
   </#if>
Oppretter:    ${document.properties.creator?html}
Opprettet:    ${document.properties.created?datetime}
Modifikator:  ${document.properties.modifier?html}
Modifisert:   ${document.properties.modified?datetime}
Størrelse:    ${document.size / 1024} kB


LENKER FOR INNHOLD

Mappe for innhold:   ${contentFolderUrl?html}
URL for innhold:     ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
URL til nedlasting:  ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
WebDAV URL:          ${contextUrl}${document.webdavUrl?html}
