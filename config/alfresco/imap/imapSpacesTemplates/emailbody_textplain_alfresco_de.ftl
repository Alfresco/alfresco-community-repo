------------------------------------------------------------------------------
Dokumentname:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titel:   ${document.properties.title}
   <#else>
Titel:          KEINER
   </#if>
   <#if document.properties.description?exists>
Beschreibung:   ${document.properties.description}
   <#else>
Beschreibung:   KEINE
   </#if>
Ersteller:   ${document.properties.creator}
Erstellt:    ${document.properties.created?datetime}
Bearbeiter:  ${document.properties.modifier}
Geändert:    ${document.properties.modified?datetime}
Größe:       ${document.size / 1024} KB


LINKS ZUM INHALT

Inhaltsordner:     ${contentFolderUrl}
URL zum Inhalt:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
URL zum Download:  ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
WebDAV-URL:        ${contextUrl}${document.webdavUrl}
