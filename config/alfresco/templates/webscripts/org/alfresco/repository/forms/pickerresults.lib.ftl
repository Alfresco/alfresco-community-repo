<#macro renderParent node indent="   ">
   <#escape x as jsonUtils.encodeJSONString(x)>
   ${indent}"parent":
   ${indent}{
   <#if (node != rootNode) && node.parent??>
      <@renderParent node.parent indent+"   " />
   </#if>
      ${indent}"type": "${node.typeShort}",
      ${indent}"isContainer": ${node.isContainer?string},
      ${indent}"hasChildren": ${(node.children?size > 0)?string},
      ${indent}"name": "${node.properties.name!""}",
      ${indent}"title": "${node.properties.title!""}",
      ${indent}"description": "${node.properties.description!""}",
      <#if node.properties.modified??>${indent}"modified": "${node.properties.modified?string("dd MMMM yyyy HH:mm")}",</#if>
      <#if node.properties.modifier??>${indent}"modifier": "${node.properties.modifier}",</#if>
      ${indent}"displayPath": "${node.displayPath!""}",
      ${indent}"nodeRef": "${node.nodeRef}"
   ${indent}},
   </#escape>
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
            "type": "${row.item.typeShort}",
            "isContainer": ${row.item.isContainer?string},
            "hasChildren": ${(row.item.children?size > 0)?string},
            "name": "${row.item.properties.name!""}",
            "title": "${row.item.properties.title!""}",
            "description": "${row.item.properties.description!""}",
            <#if row.item.properties.modified??>"modified": "${row.item.properties.modified?string("dd MMMM yyyy HH:mm")}",</#if>
            <#if row.item.properties.modifier??>"modifier": "${row.item.properties.modifier}",</#if>
            "displayPath": "${row.item.displayPath!""}",
            "nodeRef": "${row.item.nodeRef}"<#if row.selectable?exists>,
            "selectable" : ${row.selectable?string}</#if>
         }<#if row_has_next>,</#if>
      </#list>
      ]
   }
}
   </#escape>
</#macro>