<#import "item.lib.ftl" as itemLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalRecords": ${doclist.paging.totalRecords?c},
   <#if doclist.paging.totalRecordsUpper??>
   "totalRecordsUpper": ${doclist.paging.totalRecordsUpper?c},
   </#if>
   "startIndex": ${doclist.paging.startIndex?c},
   "metadata":
   {
      "repositoryId": "${server.id}",
      <#if doclist.container??>"container": "${doclist.container.nodeRef}",</#if>
      "parent":
      {
      <#if doclist.parent??>
         <#assign parentNode = doclist.parent.node>
         "nodeRef": "${parentNode.nodeRef}",
         "permissions":
         {
            "userAccess":
            {
            <#list doclist.parent.userAccess?keys as perm>
               <#if doclist.parent.userAccess[perm]?is_boolean>
               "${perm?string}": ${doclist.parent.userAccess[perm]?string}<#if perm_has_next>,</#if>
               </#if>
            </#list>
            }
         }
      </#if>
      },
      "onlineEditing": ${doclist.onlineEditing?string},
      "itemCounts":
      {
         "folders": ${(doclist.itemCount.folders!0)?c},
         "documents": ${(doclist.itemCount.documents!0)?c}
      }
   },
   "items":
   [
      <#list doclist.items as item>
      {
         <@itemLib.itemJSON item=item />
      }<#if item_has_next>,</#if>
      </#list>
   ]
}
</#escape>
