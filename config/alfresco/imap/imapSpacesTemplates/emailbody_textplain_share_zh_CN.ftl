------------------------------------------------------------------------------
文档名称：   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
标题：   ${document.properties.title}
   <#else>
标题：         无
   </#if>
   <#if document.properties.description?exists>
说明：   ${document.properties.description}
   <#else>
说明：   无
   </#if>
创建者：   ${document.properties.creator}
创建时间：   ${document.properties.created?datetime}
修改者：  ${document.properties.modifier}
修改时间：  ${document.properties.modified?datetime}
大小：      ${document.size / 1024} Kb


内容链接

内容文件夹：   ${shareContextUrl}/page/site/${parentPathFromSites}
内容 URL：      ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
下载 URL：     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
