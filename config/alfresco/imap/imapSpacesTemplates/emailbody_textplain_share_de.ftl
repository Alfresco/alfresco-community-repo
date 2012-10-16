------------------------------------------------------------------------------
Dokumentname:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titel:   ${document.properties.title}
   <#else>
Titel:         KEINER
   </#if>
   <#if document.properties.description?exists>
Beschreibung:   ${document.properties.description}
   <#else>
Beschreibung:   KEINE
   </#if>
Ersteller:   ${document.properties.creator}
Erstellt am:    ${document.properties.created?datetime}
Bearbeiter:  ${document.properties.modifier}
Bearbeitet am:  ${document.properties.modified?datetime}
Größe:       ${document.size / 1024} Kb


Links zum Inhalt

Dokumenten Ordner:   ${shareContextUrl}/page/site/${parentPathFromSites}
URL zum Inhalt:   ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Download URL:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
