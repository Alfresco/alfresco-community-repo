<#macro dateFormat date>${xmldate(date)}</#macro>
<#macro renderItem item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"name":"${item.name}",
	"nodeRef": "${item.nodeRef}",
	"type": "${item.type}",
	"displayName": "${item.displayName!''}",
	"description": "${item.description!''}",
	"createdOn": "<@dateFormat item.createdOn />",
	"createdBy": "${item.createdBy!''}",
	"createdByUser": "${item.createdByUser!''}",
	"modifiedOn": "<@dateFormat item.modifiedOn />",
	"modifiedByUser": "${item.modifiedByUser}",
	"modifiedBy": "${item.modifiedBy}",
	"size": ${item.size?c},
	"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
	<#if item.site??>
	"site":
	{
		"shortName": "${item.site.shortName}",
		"title": "${item.site.title}"
	},
	"container": "${item.container}"
	</#if>
}
</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "blogPosts": 
	{
		"items":
		[
		<#list data.blogPosts.items as item>
			<@renderItem item /><#if item_has_next>,</#if>
		</#list>
		]
	},
   "wikiPages":
	{
		"items":
		[
		<#list data.wikiPages.items as item>
			<@renderItem item /><#if item_has_next>,</#if>
		</#list>
		]
	},
   "forumPosts":
	{
		"items":
		[
		<#list data.discussions.items as item>
			<@renderItem item /><#if item_has_next>,</#if>
		</#list>
		]
	}
}
</#escape>