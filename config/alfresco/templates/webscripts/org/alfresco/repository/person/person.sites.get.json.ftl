<#import "../site/site.lib.ftl" as siteLib/>

[
	<#list sites as site>
		<@siteLib.siteJSON site=site/>
		<#if site_has_next>,</#if>
	</#list>
]