------------------------------------------------------------------------------
Имя документа:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Название:   ${document.properties.title?html}
   <#else>
Название:	ОТСУТСТВУЕТ
   </#if>
   <#if document.properties.description?exists>
Описание:   ${document.properties.description?html}
   <#else>
Описание:	ОТСУТСТВУЕТ
   </#if>
Создатель:  ${document.properties.creator?html}
Создано:    ${document.properties.created?datetime}
Редактор:   ${document.properties.modifier?html}
Изменено:   ${document.properties.modified?datetime}
Размер:     ${document.size / 1024} КБ


ССЫЛКИ

Папка с содержимым:            ${contentFolderUrl?html}
Ссылка на документ:            ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
Ссылка на загрузку документа:  ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
Ссылка WebDAV:                 ${contextUrl}${document.webdavUrl?html}
