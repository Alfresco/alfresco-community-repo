<#macro printPropertyValue p><#if p.value?is_date>"${p.value?datetime?string}"</#if><#if p.value?is_boolean>${p.value?string}</#if><#if p.value?is_number>${p.value?c}</#if><#if p.value?is_string>"${p.value}"</#if></#macro>
{
   "nodeRef": "${node.nodeRef}",
   "qnamePath": "${node.qnamePath}",
   "name": "${node.name}",
   "parentNodeRef": "<#if node.parent?exists>${node.parent.nodeRef}</#if>",
   "type": "${node.type}",
   "typeShort": "${node.typeShort}",
   "id": "${node.id}",
   "nodeRef": "${node.nodeRef}",
   "aspects": [
   <#list node.aspects as aspect>
      "${aspect}"<#if aspect_has_next>,</#if>
   </#list>
   ],
   "properties": [
   <#list properties as p>
      {
         "name": "${p.name}",
         "value": <@printPropertyValue p />,
         "type": "${p.type}"
      }<#if p_has_next>,</#if>
   </#list>
   ],
   "children": [
   <#list children as child>
      {
         "name": "${child.name}",
         "nodeRef": "${child.nodeRef}",
         "type": "${child.type}",
         "assocType": "${child.assocType}"
      }<#if child_has_next>,</#if>
   </#list>
   ],
   "parents": [
   <#list parents as p>
      {
         "name": "${p.name}",
         "nodeRef": "${p.nodeRef}",
         "type": "${p.type}",
         "assocType": "${p.assocType}"
      }<#if p_has_next>,</#if>
   </#list>
   ],
   "assocs": [
   <#list assocs as p>
      {
         "name": "${p.name}",
         "nodeRef": "${p.nodeRef}",
         "type": "${p.type}",
         "assocType": "${p.assocType}"
      }<#if p_has_next>,</#if>
   </#list>
   ],
   "permissions": {
      "entries": [
      <#list permissions.entries as p>
         {
            "permission": "${p.permission}",
            "authority": "${p.authority}",
            "rel": "${p.rel}"
         }<#if p_has_next>,</#if>
      </#list>
      ],
      "inherit": ${permissions.inherit?string},
      "owner": "<#if permissions.owner?exists>${permissions.owner?js_string}</#if>"
   }
}