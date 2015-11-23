------------------------------------------------------------------------------
Nome do documento:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Título:   ${document.properties.title}
   <#else>
Título:       NENHUM
   </#if>
   <#if document.properties.description?exists>
Descrição:   ${document.properties.description}
   <#else>
Descrição:    NENHUMA
   </#if>
Criador:      ${document.properties.creator}
Criado:       ${document.properties.created?datetime}
Modificador:  ${document.properties.modifier}
Modificado:   ${document.properties.modified?datetime}
Tamanho:      ${document.size / 1024} KB


LINKS DE CONTEÚDO

Pasta de conteúdo:  ${contentFolderUrl}
URL de conteúdo:    ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
URL de download:    ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
