<#import "item.lib.ftl" as itemLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "metadata":
   {
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
      "onlineEditing": ${doclist.onlineEditing?string}
   },
   "item":
   {
      <#if doclist.items??><@itemLib.itemJSON item=doclist.items[0] /></#if>
   }
}
</#escape>
