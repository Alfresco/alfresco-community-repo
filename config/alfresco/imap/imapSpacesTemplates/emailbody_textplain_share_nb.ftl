------------------------------------------------------------------------------
Dokumentnavn:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Tittel:   ${document.properties.title}
   <#else>
Tittel:       INGEN
   </#if>
   <#if document.properties.description?exists>
Beskrivelse:   ${document.properties.description}
   <#else>
Beskrivelse:  INGEN
   </#if>
Oppretter:    ${document.properties.creator}
Opprettet:    ${document.properties.created?datetime}
Modifikator:  ${document.properties.modifier}
Modifisert:   ${document.properties.modified?datetime}
Størrelse:    ${document.size / 1024} kB


LENKER FOR INNHOLD

Mappe for innhold:   ${contentFolderUrl}
URL for innhold:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
URL til nedlasting:  ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
