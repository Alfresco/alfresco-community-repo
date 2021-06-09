<#macro dateFormat date>${date?string("yyyy-MM-dd'T'HH:mm:ss.SSSZ")}</#macro>
<#macro formatDataItems data>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			"description": "${item.description!''}",
			"createdOn": "<@dateFormat item.createdOn />",
			"createdBy": "${item.createdBy!''}",
			"createdByUser": "${item.createdByUser!''}",
			"modifiedOn": "<@dateFormat item.modifiedOn />",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"size": ${item.size?c},
			<#if item.site??>"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},</#if>
			"container": "${item.container!""}",
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>]
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>
</#macro>
{
	"created": <@formatDataItems data['created'] />,
	"modified": <@formatDataItems data['modified'] />
}