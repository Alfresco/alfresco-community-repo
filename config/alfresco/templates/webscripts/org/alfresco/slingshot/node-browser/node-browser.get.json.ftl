<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
<#macro printPropertyValue p><#if p.value??><#if p.value?is_date>"<@dateFormat p.value />"</#if><#if p.value?is_boolean>${p.value?string}</#if><#if p.value?is_number>${p.value?c}</#if><#if p.value?is_string>"${p.value}"</#if><#else>null</#if></#macro>
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
      		}<#if val_has_next>,</#if>
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