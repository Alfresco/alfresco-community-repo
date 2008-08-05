<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items":
   [
      <#list data.items as item>
      {
         "index": ${item_index},
         "nodeRef" : "${item.nodeRef}",
         "qnamePath" : "${item.qnamePath}",
         "type": "${item.type}",
         "icon32": "${item.icon32}",
         "name" : "${item.name!''}",
         "displayName": "${item.displayName!''}",
         "tags" : [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
         <#if item.downloadUrl??>
         "downloadUrl" : "${item.downloadUrl}",
         </#if>
         <#if item.browseUrl??>
         "browseUrl" : "${item.browseUrl}",
         </#if>
         "site" : {
           "shortName" : "${item.site.shortName}",
           "title" : "${item.site.title}"
         },
         "container" : "${item.container}"
      }<#if item_has_next>,</#if>
      </#list>
   ]
}
</#escape>
