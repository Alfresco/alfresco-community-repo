<#macro renderParent node indent="   ">
   ${indent}"parent":
   ${indent}{
   <#if (node != rootNode) && node.parent??>
      <@renderParent node.parent indent+"   " />
   </#if>
      ${indent}"type": "${node.typeShort}",
      ${indent}"hasChildren": ${(node.children?size > 0)?string},
      ${indent}"name": "${node.properties.name!""}",
      ${indent}"description": "${node.properties.description!""}",
      ${indent}"displayPath": "${node.displayPath!""}",
      ${indent}"nodeRef": "${node.nodeRef}"
   ${indent}},
</#macro>

<#macro pickerResultsJSON results>
   <#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
<#if parent??>
   <@renderParent parent />
</#if>
      "items":
      [
      <#list results as row>
         {
            "type": "${row.typeShort}",
            "hasChildren": ${(row.children?size > 0)?string},
            "name": "${row.properties.name!""}",
            "description": "${row.properties.description!""}",
            "displayPath": "${row.displayPath!""}",
            "hasChildren": ${(row.children?size > 0)?string},
            "nodeRef": "${row.nodeRef}"
         }<#if row_has_next>,</#if>
      </#list>
      ]
   }
}
   </#escape>
</#macro>