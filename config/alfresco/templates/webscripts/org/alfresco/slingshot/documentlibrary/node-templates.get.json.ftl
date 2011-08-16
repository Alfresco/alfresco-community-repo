<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   [
   <#list nodes as node>
      {
         "nodeRef": "${node.nodeRef}",
         "name": "${node.name}",
         "title": "${node.properties.title!""}",
         "description": "${node.properties.description!""}"
      }<#if node_has_next>,</#if>
   </#list>
   ]
}
</#escape>