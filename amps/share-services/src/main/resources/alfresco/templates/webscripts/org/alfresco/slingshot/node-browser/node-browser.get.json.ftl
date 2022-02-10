<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
<#assign maxDepth=1000 />
<#macro printPropertyValue p>
   <#attempt>
      <#if p.value??>
        <#if p.value?is_date>
         "<@dateFormat p.value />"
        <#elseif p.value?is_boolean>
         ${p.value?string}
        <#elseif p.value?is_number>
         ${p.value?c}
        <#elseif p.value?is_string>
         "${p.value}"
        <#elseif p.value?is_hash || p.value?is_enumerable>
            <#assign val>
               <@convertToJSON p.value />
            </#assign>
            "${val}"
        </#if>
   	  <#else>
   	     null
      </#if>
   <#recover>
      "${.error}"
   </#attempt>
</#macro>
<#macro convertToJSON v>
   <#if v??>
      <#if v?is_date>
         "<@dateFormat v />"
      <#elseif v?is_boolean>
         ${v?string}
      <#elseif v?is_number>
         ${v?c}
      <#elseif v?is_string>
         "${v?string}"
      <#elseif v?is_hash>
         <#if v?keys?size gt maxDepth >
            <#stop "Max depth of object achieved">
         </#if>
         <@compress single_line=true>
            {
            <#assign first = true />
            <#list v?keys as key>
               <#if first = false>,</#if>
               "${key}":
               <#if v[key]??>
                  <@convertToJSON v[key] />
               <#else>
                  null
               </#if>
               <#assign first = false/>
            </#list>
            }
         </@compress>
      <#elseif v?is_enumerable>
         <#if v?size gt maxDepth>
            <#stop "Max depth of object achieved" >
         </#if>
         <#assign first = true />
            <@compress single_line=true>
               [
               <#list v as item>
                  <#if first = false>,</#if>
                  <@convertToJSON item />
                  <#assign first = false/>
               </#list>
               ]
            </@compress>
      <#else>
         ${v}
      </#if>
   <#else>
      null
   </#if>
</#macro>
{
   <#if node??>
   "nodeRef": "${node.nodeRef}",
   "qnamePath": {
      "name": "${node.qnamePath}",
      "prefixedName": "${node.prefixedQNamePath}"
   },
   "name": {
      "name": "${node.name}",
      "prefixedName": "${node.prefixedName}"
   },
   "parentNodeRef": "<#if node.parentNodeRef?exists>${node.parentNodeRef}</#if>",
   "type": {
      "name": "${node.type.name}",
      "prefixedName": "${node.type.prefixedName}"
   },
   "id": "${node.id}",
   "nodeRef": "${node.nodeRef}",
   "aspects": [
   <#list aspects as aspect>
      {
         "name": "${aspect.name}",
         "prefixedName": "${aspect.prefixedName}"
      }
      <#if aspect_has_next>,</#if>
   </#list>
   ],
   "properties": [
   <#list properties as p>
      {
         "name": {
            "name": "${p.name.name}",
            "prefixedName": "${p.name.prefixedName}"
         },
         "values": [
            <#list p.values as val>
            {
               "dataType": "${val.dataType!""}",
               "value": <@printPropertyValue val />,
               "isContent": ${val.content?string},
               "isNodeRef": ${val.nodeRef?string},
               "isNullValue": ${val.nullValue?string}
            }
            <#if val_has_next>,</#if>
            </#list>
         ],
         "type": {
            "name": "<#if p.typeName??>${p.typeName.name}</#if>",
            "prefixedName": "<#if p.typeName??>${p.typeName.prefixedName}</#if>"
         },
         "multiple": ${p.collection?string},
         "residual": ${p.residual?string}
      }<#if p_has_next>,</#if>
   </#list>
   ],
   "children": [
   <#list children as child>
      {
         "name": {
            "name": "${child.name.name}",
            "prefixedName": "${child.name.prefixedName}"
         },
         "nodeRef": "${child.childRef}",
         "type": {
            "name": "${child.childTypeName.name}",
            "prefixedName": "${child.childTypeName.prefixedName}"
         },
         "assocType": {
            "name": "${child.typeName.name}",
            "prefixedName": "${child.typeName.prefixedName}"
         },
         "primary": ${child.primary?string},
         "index": ${child_index?c}
      }<#if child_has_next>,</#if>
   </#list>
   ],
   "parents": [
   <#list parents as p>
      {
         "name": {
            "name": "${p.name.name}",
            "prefixedName": "${p.name.prefixedName}"
         },
         "nodeRef": "${p.parentRef}",
         "type": {
            "name": "${p.parentTypeName.name}",
            "prefixedName": "${p.parentTypeName.prefixedName}"
         },
         "assocType": {
            "name": "${p.typeName.name}",
            "prefixedName": "${p.typeName.prefixedName}"
         },
         "primary": ${p.primary?string}
      }<#if p_has_next>,</#if>
   </#list>
   ],
   "assocs": [
   <#list assocs as assoc>
      {
         "type": {
            "name": "${assoc.targetTypeName.name}",
            "prefixedName": "${assoc.targetTypeName.prefixedName}"
         },
         "sourceRef": "${assoc.sourceRef}",
         "targetRef": "${assoc.targetRef}",
         "assocType": {
            "name": "${assoc.typeName.name}",
            "prefixedName": "${assoc.typeName.prefixedName}"
         }
      }<#if assoc_has_next>,</#if>
   </#list>
   ],
   "sourceAssocs": [
   <#if sourceAssocs??>
   <#list sourceAssocs as assoc>
      {
         "type": {
            "name": "${assoc.sourceTypeName.name}",
            "prefixedName": "${assoc.sourceTypeName.prefixedName}"
         },
         "sourceRef": "${assoc.sourceRef}",
         "targetRef": "${assoc.targetRef}",
         "assocType": {
            "name": "${assoc.typeName.name}",
            "prefixedName": "${assoc.typeName.prefixedName}"
         }
      }<#if assoc_has_next>,</#if>
   </#list>
   </#if>
   ],
   "permissions": {
      "entries": [
      <#list permissions.entries as p>
         {
            "permission": "${p.permission}",
            "authority": "${p.authority}",
            "rel": "${p.accessStatus}"
         }<#if p_has_next>,</#if>
      </#list>
      ],
      "masks": [
      <#list permissions.storePermissions as p>
         {
            "permission": "${p.permission}",
            "authority": "${p.authority}",
            "rel": "${p.accessStatus}"
         }<#if p_has_next>,</#if>
      </#list>
      ],
      "inherit": ${permissions.inherit?string},
      "owner": "<#if permissions.owner?exists>${permissions.owner}</#if>"
   }
   <#elseif results??>
   "numResults": ${results?size?c},
   "results": [
   <#list results as result>
      <#assign qnamePath=result.qnamePath />
      {
         "nodeRef": "${result.nodeRef}",
         "qnamePath": {
            "name": "${result.qnamePath}",
            "prefixedName": "${result.prefixedQNamePath}"
         },
         "name": {
            "name": "${result.name}",
            "prefixedName": "${result.prefixedName}"
         },
         "parentNodeRef": "<#if result.parent??>${result.parent.nodeRef}</#if>"
      }<#if result_has_next>,</#if>
   </#list>
   ],
   "searchElapsedTime": ${(searchElapsedTime!0)?c}
   <#elseif stores??>
   "stores": [
      <#list stores as store>"${store}"<#if store_has_next>,</#if>
   </#list>
   ]
   </#if>
}
</#escape>