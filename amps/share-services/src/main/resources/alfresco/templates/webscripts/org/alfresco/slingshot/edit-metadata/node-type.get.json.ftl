<#macro nodeInfo node name>
<#escape x as jsonUtils.encodeJSONString(x)>
   "${name}":
   {
      "nodeRef": "${node.nodeRef}",
      "type": "${node.typeShort}",
      "isContainer": ${node.isContainer?string},
      "fileName": "${node.name}"
   }
</#escape>
</#macro>
{
   <#if parent??><@nodeInfo parent "parent" />,</#if>
   <@nodeInfo node "node" />
}
