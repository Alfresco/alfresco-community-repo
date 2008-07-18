<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
{
   "items":
   [
      <#list data.items as item>
      {
         "index": ${item_index},
         "nodeRef" : "${item.nodeRef?j_string}",
         "qnamePath" : "${item.qnamePath?j_string}",
         "type": "${item.type}",
         "icon32": "${item.icon32}",
         "name" : "${(item.name!'')?j_string}",
         "displayName": "${(item.displayName!'')?j_string}",
         "tags" : [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
         <#if item.downloadUrl??>
         "downloadUrl" : "${item.downloadUrl?j_string}",
         </#if>
         <#if item.browseUrl??>
         "browseUrl" : "${item.browseUrl?j_string}",
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
      <#--
      we will expand the API for get/create container nodes to accept a number
      of URL template strings with well known ids, so say for Forums it will have
      to supply URI template for View, View Details, View Component for an item
      -->