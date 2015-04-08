<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalResults": ${categorynode.items?size?c},
   "items":
   [
   <#list categorynode.items as item>
      <#assign c = item.node>
      {
         "nodeRef": "${c.nodeRef}",
         "name": "${c.name}",
         "description": "${(c.properties.description!"")}",
         "hasChildren": ${item.hasSubfolders?string},
         "userAccess":
         {
            "create": ${c.hasPermission("CreateChildren")?string},
            "edit": ${c.hasPermission("Write")?string},
            "delete": ${c.hasPermission("Delete")?string}
         }
      }<#if item_has_next>,</#if>
   </#list>
   ]
}
</#escape>
