<#import "site.lib.ftl" as siteLib/>

[
	<#list sites?sort_by("shortName") as site>
		<@siteLib.siteJSONManagers site=site roles=roles>
		"isMemberOfGroup": ${site.isMemberOfGroup(person.properties.userName)?string},
		</@siteLib.siteJSONManagers>
		<#if site_has_next>,</#if>
	</#list>
]