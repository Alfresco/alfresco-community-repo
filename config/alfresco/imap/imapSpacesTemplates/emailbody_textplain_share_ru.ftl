------------------------------------------------------------------------------
Имя документа:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Заголовок:   ${document.properties.title}
   <#else>
Заголовок:	НЕТ
   </#if>
   <#if document.properties.description?exists>
Описание:   ${document.properties.description}
   <#else>
Описание:	НЕТ
   </#if>
Создатель:   ${document.properties.creator}
Создано:   ${document.properties.created?datetime}
Модификатор:  ${document.properties.modifier}
Изменено:  ${document.properties.modified?datetime}
Размер:      ${document.size / 1024} КБ


ССЫЛКИ НА КОНТЕНТ

Папка с контентом:   ${shareContextUrl}/page/site/${parentPathFromSites}
URL-адрес контента:      ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Загрузить с URL-адреса:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
