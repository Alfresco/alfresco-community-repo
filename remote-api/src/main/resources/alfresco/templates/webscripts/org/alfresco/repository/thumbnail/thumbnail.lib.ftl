<#macro thumbnailJSON node thumbnailName>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "thumbnailName" : "${thumbnailName}",
   "url" : "${url.serviceContext + "/api/node/" + node.storeType + "/" + node.storeId+ "/" + node.id + "/content/thumbnails/" + thumbnailName}"
}
</#escape>
</#macro>