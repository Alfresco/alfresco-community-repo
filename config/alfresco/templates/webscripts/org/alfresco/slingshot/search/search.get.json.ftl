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
         "displayName": "${item.name?replace(workingCopyLabel, "")?html}",
         "tags" : [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
         "title" : "${(item.title!'')?j_string}",
         "viewUrl" : "${item.viewUrl?j_string}",
         "detailsUrl" : "${item.detailsUrl?j_string}",
         "componentUrl" : "${item.containerUrl?j_string}",
         "site" : "${item.site}",
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