<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
		<#list items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name}",
			"title": "${item.title!''}",
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"createdOn": "${xmldate(item.createdOn)}",
			"createdByUser": "${item.createdByUser}",
			"createdBy": "${item.createdBy}",
			"author": "${item.author!''}",
			"size": ${item.size?c},
			<#if item.browseUrl??>"browseUrl": "${item.browseUrl}",</#if>
			"parentFolder": "${item.parentFolder!""}",
			"properties":
			{
			<#assign first=true>
			<#list item.properties?keys as k>
				<#if item.properties[k]??>
					<#if !first>,<#else><#assign first=false></#if>"${k}":
					<#assign prop = item.properties[k]>
					<#if prop?is_date>"${xmldate(prop)}"
					<#elseif prop?is_boolean>${prop?string("true", "false")}
					<#elseif prop?is_enumerable>[<#list prop as p>"${p}"<#if p_has_next>, </#if></#list>]
					<#elseif prop?is_number>${prop?c}
					<#else>"${prop}"
					</#if>
				</#if>
			</#list>
			}
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>