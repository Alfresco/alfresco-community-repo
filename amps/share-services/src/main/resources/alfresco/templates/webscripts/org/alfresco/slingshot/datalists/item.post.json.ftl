<#import "item.lib.ftl" as itemLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
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
   "item":
   {
      <@itemLib.itemJSON data.item />
   }
}
</#escape>