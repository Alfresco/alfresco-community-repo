<#assign p = treenode.parent>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalResults": ${treenode.items?size?c},
   "resultsTrimmed": ${treenode.resultsTrimmed?string},
   "parent":
   {
      "nodeRef": "${p.nodeRef}",
      "userAccess":
      {
         "create": ${p.hasPermission("CreateChildren")?string},
         "edit": ${p.hasPermission("Write")?string},
         "delete": ${p.hasPermission("Delete")?string}
      }
   },
   "items":
   [
   <#list treenode.items as item>
      <#assign t = item.node>
      {
         "nodeRef": "${t.nodeRef}",
         "name": "${t.name}",
         "description": "${(t.properties.description!"")}",
         "hasChildren": ${item.hasSubfolders?string},
         "userAccess":
         {
            "create": ${t.hasPermission("CreateChildren")?string},
            "edit": ${t.hasPermission("Write")?string},
            "delete": ${t.hasPermission("Delete")?string}
         },
         "aspects": 
         [
         <#list item.aspects as aspect>
            "${aspect}"<#if aspect_has_next>,</#if>
         </#list>
         ]
      }<#if item_has_next>,</#if>
   </#list>
   ]
}
</#escape>
