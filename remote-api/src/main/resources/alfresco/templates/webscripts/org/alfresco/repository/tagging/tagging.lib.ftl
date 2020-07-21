<#macro tagJSON item>
{
   "name" : "${jsonUtils.encodeJSONString(item.name)}",
   "count" : ${item.count?c}
}
</#macro>

<#macro tagJSONDetails item>
{
   "type": "${item.typeShort}",
	"isContainer": ${item.isContainer?string},
	"name": "${item.properties.name!""}",
	"title": "${item.properties.title!""}",
	"description": "${item.properties.description!""}",
	<#if item.properties.modified??>"modified": "${xmldate(item.properties.modified)}",</#if>
	<#if item.properties.modifier??>"modifier": "${item.properties.modifier}",</#if>
	<#if item.siteShortName??>"site": "${item.siteShortName}",</#if>
	"displayPath": "${item.displayPath!""}",
	"nodeRef": "${item.nodeRef}",
	"selectable" : true
}
</#macro>