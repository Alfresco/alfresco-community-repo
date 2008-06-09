<#if node?exists>
{
   "thumbnailName" : "${thumbnailName}",
   "url" : "${url.serviceContext}/api/node/${node.storeType}/${node.storeId}/${node.id}/content/thumbnails/${thumbnailName}"
}
</#if>