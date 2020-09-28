------------------------------------------------------------------------------
文件名：   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
标题：   ${document.properties.title?html}
   <#else>
标题：   无
   </#if>
   <#if document.properties.description?exists>
说明：   ${document.properties.description?html}
   <#else>
说明：   无
   </#if>
创建者：    ${document.properties.creator?html}
创建时间：  ${document.properties.created?datetime}
修改者：    ${document.properties.modifier?html}
修改时间：  ${document.properties.modified?datetime}
大小：      ${document.size / 1024} KB


内容链接

内容文件夹:   ${contentFolderUrl?html}
URL 内容:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
URL 下载:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
URL WebDAV:  ${contextUrl}${document.webdavUrl?html}
