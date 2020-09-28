------------------------------------------------------------------------------
Document name:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Title:   ${document.properties.title?html}
   <#else>
Title:         NONE
   </#if>
   <#if document.properties.description?exists>
Description:   ${document.properties.description?html}
   <#else>
Description:   NONE
   </#if>
Creator:   ${document.properties.creator?html}
Created:   ${document.properties.created?datetime}
Modifier:  ${document.properties.modifier?html}
Modified:  ${document.properties.modified?datetime}
Size:      ${document.size / 1024} Kb


CONTENT LINKS

Content folder:   ${contentFolderUrl?html}
Content URL:      ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
Download URL:     ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
WebDAV URL:       ${contextUrl}${document.webdavUrl?html}
