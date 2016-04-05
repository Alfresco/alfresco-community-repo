------------------------------------------------------------------------------
Nome do documento:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Título:   ${document.properties.title?html}
   <#else>
Título:       NENHUM
   </#if>
   <#if document.properties.description?exists>
Descrição:   ${document.properties.description?html}
   <#else>
Descrição:    NENHUMA
   </#if>
Criador:      ${document.properties.creator?html}
Criado:       ${document.properties.created?datetime}
Modificador:  ${document.properties.modifier?html}
Modificado:   ${document.properties.modified?datetime}
Tamanho:      ${document.size / 1024} KB


LINKS DE CONTEÚDO

Pasta de conteúdo:  ${contentFolderUrl?html}
URL de conteúdo:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
URL de download:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
URL de WebDAV:      ${contextUrl}${document.webdavUrl?html}
