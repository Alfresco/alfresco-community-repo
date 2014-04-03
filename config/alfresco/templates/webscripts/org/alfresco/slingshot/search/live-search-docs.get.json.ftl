<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${data.totalRecords?c},
	"startIndex": ${data.startIndex?c},
	"hasMoreRecords": ${data.hasMoreRecords?c},
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"name": "${item.name!''}",
			"title": "${item.title!''}",
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedBy": "${item.modifiedBy}",
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.lastThumbnailModification??>
			"lastThumbnailModification": "${item.lastThumbnailModification}",
			</#if>
			"size": ${item.size?c},
			"mimetype": "${item.mimetype!''}"
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>