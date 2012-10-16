------------------------------------------------------------------------------
Nombre del documento:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Título:   ${document.properties.title}
   <#else>
Título:         NINGUNO
   </#if>
   <#if document.properties.description?exists>
Descripción:   ${document.properties.description}
   <#else>
Descripción:   NINGUNA
   </#if>
Creador:   ${document.properties.creator}
Creado:   ${document.properties.created?datetime}
Modificador:  ${document.properties.modifier}
Modificado:  ${document.properties.modified?datetime}
Tamaño:      ${document.size / 1024} Kb


Enlaces de contenido

Carpeta de contenido:   ${shareContextUrl}/page/site/${parentPathFromSites}
Dirección URL de contenido:    ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Dirección URL de descarga:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
