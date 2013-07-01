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

Папка с контентом:   ${contextUrl}/navigate/browse${document.parent.webdavUrl}
URL-адрес контента:      ${contextUrl}${document.url}
Загрузить с URL-адреса:     ${contextUrl}${document.downloadUrl}
URL-адрес WebDAV:       ${contextUrl}${document.webdavUrl}
