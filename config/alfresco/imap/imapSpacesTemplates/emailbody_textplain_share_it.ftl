------------------------------------------------------------------------------
Nome documento:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titolo:      ${document.properties.title}
   <#else>
Titolo:         NESSUNO
   </#if>
   <#if document.properties.description?exists>
Descrizione:   ${document.properties.description}
   <#else>
Descrizione:   NESSUNO
   </#if>
Autore:              ${document.properties.creator}
Data di creazione:   ${document.properties.created?datetime}
Modificatore:        ${document.properties.modifier}
Data di modifica:    ${document.properties.modified?datetime}
Dimensioni:          ${document.size / 1024} Kb


Collegamenti del contenuto

Cartella del contenuto:   ${shareContextUrl}/page/site/${parentPathFromSites}
URL del contenuto:      ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
URL di download:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
