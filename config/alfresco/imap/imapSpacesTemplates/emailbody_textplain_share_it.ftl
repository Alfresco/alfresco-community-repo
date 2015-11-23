------------------------------------------------------------------------------
Nome documento:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titolo:      ${document.properties.title}
   <#else>
Titolo:        NESSUNO
   </#if>
   <#if document.properties.description?exists>
Descrizione:   ${document.properties.description}
   <#else>
Descrizione:   NESSUNA
   </#if>
Creatore:      ${document.properties.creator}
Creato:        ${document.properties.created?datetime}
Modificatore:  ${document.properties.modifier}
Modificato:    ${document.properties.modified?datetime}
Dimensioni:    ${document.size / 1024} KB


LINK AL CONTENUTO

Cartella del contenuto:  ${contentFolderUrl}
URL del contenuto:       ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
URL di download:         ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
