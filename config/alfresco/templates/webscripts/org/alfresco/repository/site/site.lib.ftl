<#macro siteJSON site>
{
	"url" : "${url.serviceContext}/api/sites/${site.shortName}",
	"sitePreset" : "${site.sitePreset}",
	"shortName" : "${site.shortName}",
	"title" : "${site.title}",
	"description" : "${site.description}",
	<#if site.node?exists>
	   "node" : "${url.serviceContext}/api/node/${site.node.storeType}/${site.node.storeId}/${site.node.id}",
	   "tagScope" : "${url.serviceContext}/api/tagscopes/${site.node.storeType}/${site.node.storeId}/${site.node.id}",
	</#if>
	"isPublic" : ${site.isPublic?string("true", "false")}
}
</#macro>