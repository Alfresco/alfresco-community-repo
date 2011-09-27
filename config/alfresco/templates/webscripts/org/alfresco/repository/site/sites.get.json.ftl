<#import "site.lib.ftl" as siteLib/>

[
	<#list sites?sort_by("shortName") as site>
		<@siteLib.siteJSONManagers site=site roles=roles/>
		<#if site_has_next>,</#if>
	</#list>
]