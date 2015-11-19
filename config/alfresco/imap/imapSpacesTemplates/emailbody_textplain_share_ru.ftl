------------------------------------------------------------------------------
Имя документа:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Название:   ${document.properties.title}
   <#else>
Название:	ОТСУТСТВУЕТ
   </#if>
   <#if document.properties.description?exists>
Описание:   ${document.properties.description}
   <#else>
Описание:	ОТСУТСТВУЕТ
   </#if>
Создатель:  ${document.properties.creator}
Создано:    ${document.properties.created?datetime}
Редактор:   ${document.properties.modifier}
Изменено:   ${document.properties.modified?datetime}
Размер:     ${document.size / 1024} КБ


ССЫЛКИ

Папка с содержимым:            ${contentFolderUrl}
Ссылка на документ:            ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Ссылка на загрузку документа:  ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
