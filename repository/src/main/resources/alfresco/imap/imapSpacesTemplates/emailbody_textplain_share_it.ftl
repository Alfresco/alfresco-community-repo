------------------------------------------------------------------------------
Nome documento:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titolo:      ${document.properties.title?html}
   <#else>
Titolo:        NESSUNO
   </#if>
   <#if document.properties.description?exists>
Descrizione:   ${document.properties.description?html}
   <#else>
Descrizione:   NESSUNA
   </#if>
Creatore:      ${document.properties.creator?html}
Creato:        ${document.properties.created?datetime}
Modificatore:  ${document.properties.modifier?html}
Modificato:    ${document.properties.modified?datetime}
Dimensioni:    ${document.size / 1024} KB


LINK AL CONTENUTO

Cartella del contenuto:  ${contentFolderUrl?html}
URL del contenuto:       ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
URL di download:         ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
