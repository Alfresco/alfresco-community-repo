<#import "item.lib.ftl" as itemLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalRecords": ${data.paging.totalRecords?c},
   "startIndex": ${data.paging.startIndex?c},
   "metadata":
   {
      "parent":
      {
      <#if data.parent??>
         <#assign parentNode = data.parent.node>
         "nodeRef": "${parentNode.nodeRef}",
         "permissions":
         {
            "userAccess":
            {
            <#list data.parent.userAccess?keys as perm>
               <#if data.parent.userAccess[perm]?is_boolean>
               "${perm?string}": ${data.parent.userAccess[perm]?string}<#if perm_has_next>,</#if>
               </#if>
            </#list>
            }
         }
      </#if>
      }
   },
   "items":
   [
      <#list data.items as item>
      {
         <@itemLib.itemJSON item />
      }<#if item_has_next>,</#if>
      </#list>
   ]
}
</#escape>