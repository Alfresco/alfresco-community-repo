<#macro siteJSON site>
<@siteJSONManagers site=site roles="managers"/>
</#macro>

<#macro siteJSONManagers site roles>
<#local userid=person.properties.userName>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"url": "${url.serviceContext + "/api/sites/" + site.shortName}",
	"sitePreset": "${site.sitePreset}",
	"shortName": "${site.shortName}",
	"title": "${site.title}",
	"description": "${site.description}",
	<#if site.node?exists>
	"node": "${url.serviceContext + "/api/node/" + site.node.storeType + "/" + site.node.storeId + "/" + site.node.id}",
	"tagScope": "${url.serviceContext + "/api/tagscopes/" + site.node.storeType + "/" + site.node.storeId + "/" + site.node.id}",
	</#if>
	<#if site.customProperties?size != 0>
	"customProperties":
	{
		<#list site.customProperties?keys as prop>
		<#assign propDetails = site.customProperties[prop]>
		"${prop}":
		{
			"name": "${prop}",
			"value":
			<#if propDetails.value?is_enumerable>
			[
			<#list propDetails.value as v>
			"${v?string}"<#if v_has_next>,</#if>
			</#list>
			]
			<#else>
			"${propDetails.value?string}"
			</#if>,
			"type": <#if propDetails.type??>"${propDetails.type}"<#else>null</#if>, 
			"title": <#if propDetails.title??>"${propDetails.title}"<#else>null</#if>
		}
		<#if prop_has_next>,</#if>
		</#list>
	},
	</#if>
	<#if roles = "user">
	"siteRole": "${site.getMembersRole(userid)!""}",
	<#elseif roles = "managers">
	"siteManagers":
	[
		<#assign managers = site.listMembers(null, "SiteManager", 0, true)?keys />
		<#list managers as manager>
			"${manager}"<#if manager_has_next>,</#if>
		</#list>
	],
	</#if>
	<#nested>
	"isPublic": ${site.isPublic?string("true", "false")},
	"visibility": "${site.visibility}"
}
</#escape>
</#macro>
