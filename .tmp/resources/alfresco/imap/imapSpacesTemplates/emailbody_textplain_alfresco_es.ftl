------------------------------------------------------------------------------
Nombre del documento:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Título:   ${document.properties.title?html}
   <#else>
Título:       NINGUNO
   </#if>
   <#if document.properties.description?exists>
Descripción:   ${document.properties.description?html}
   <#else>
Descripción:  NINGUNA
   </#if>
Creador:   ${document.properties.creator?html}
Creado:       ${document.properties.created?datetime}
Modificador:  ${document.properties.modifier?html}
Modificado:   ${document.properties.modified?datetime}
Tamaño:       ${document.size / 1024} KB


ENLACES DE CONTENIDO

Carpeta de contenido:  ${contentFolderUrl?html}
URL de contenido:      ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
URL de descarga:       ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
URL de WebDAV:         ${contextUrl}${document.webdavUrl?html}
