<#assign first = true/>
[
	<#list sites as site>
		<#if first == true><#assign first = false/><#else>,</#if>
		{
			"url" : "${url.serviceContext}/api/sites/${site.shortName}",
			"sitePreset" : "${site.sitePreset}",
			"shortName" : "${site.shortName}",
			"title" : "${site.title}",
			"description" : "${site.description}",
			"isPublic" : ${site.isPublic?string("true", "false")}
		}
	</#list>
]