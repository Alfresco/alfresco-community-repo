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
         "name" : "${(item.name!'')?html?j_string}",
         "displayName": "${(item.displayName!'')?html?j_string}",
         "tags" : [<#list item.tags as tag>"${tag?html?j_string}"<#if tag_has_next>,</#if></#list>],
         <#if item.downloadUrl??>
         "downloadUrl" : "${item.downloadUrl?j_string}",
         </#if>
         <#if item.browseUrl??>
         "browseUrl" : "${item.browseUrl?j_string}",
         </#if>
         "site" : {
           "shortName" : "${item.site.shortName?html?j_string}",
           "title" : "${item.site.title?html?j_string}"
         },
         "container" : "${item.container?html?j_string}"
      }<#if item_has_next>,</#if>
      </#list>
   ]
}
