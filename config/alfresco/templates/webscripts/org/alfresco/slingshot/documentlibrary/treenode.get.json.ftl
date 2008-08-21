<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalResults": ${treenode.items?size?c},
   "items":
   [
   <#list treenode.items as t>
      <#assign hasChildren = false>
      <#list t.children as c>
         <#if c.isContainer><#assign hasChildren = true><#break></#if>
      </#list>
      {
         "nodeRef": "${t.nodeRef}",
         "name": "${t.name}",
         "description": "${(t.properties.description!"{}")}",
         "hasChildren": ${hasChildren?string},
         "userAccess":
      	{
      	   "create": ${t.hasPermission("CreateChildren")?string},
      	   "edit": ${t.hasPermission("Write")?string},
      	   "delete": ${t.hasPermission("Delete")?string}
      	}
      }<#if t_has_next>,</#if>
   </#list>
   ]
}
</#escape>
