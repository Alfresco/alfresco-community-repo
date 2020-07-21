<#import "../site/site.lib.ftl" as siteLib/>

[
	<#list sites as site>
		<@siteLib.siteJSONManagers site=site roles=roles/>
		<#if site_has_next>,</#if>
	</#list>
]