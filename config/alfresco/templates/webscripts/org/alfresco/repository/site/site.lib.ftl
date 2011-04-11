<#macro siteJSON site>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"url" : "${url.serviceContext + "/api/sites/" + site.shortName}",
	"sitePreset" : "${site.sitePreset}",
	"shortName" : "${site.shortName}",
	"title" : "${site.title}",
	"description" : "${site.description}",
	<#if site.node?exists>
		"node" : "${url.serviceContext + "/api/node/" + site.node.storeType + "/" + site.node.storeId + "/" + site.node.id}",
		"tagScope" : "${url.serviceContext + "/api/tagscopes/" + site.node.storeType + "/" + site.node.storeId + "/" + site.node.id}",
	</#if>
	"isPublic" : ${site.isPublic?string("true", "false")},
	"visibility" : "${site.visibility}",
	<#if site.customProperties?size != 0>
		"customProperties" :
		{
			<#list site.customProperties?keys as prop>
				"${prop}" : 
				{
					"name" : "${prop}",
					"value" : "${site.customProperties[prop].value?string}",
					"type" : "${site.customProperties[prop].type}",
					"title" : "${site.customProperties[prop].title}"
				}
				<#if prop_has_next>,</#if>
			</#list>	
		},
	</#if>
	"siteManagers" : 
	[			
		<#assign managers = site.listMembers(null, "SiteManager", 0, true)?keys />
      <#list managers as manager>              
         "${manager}"  
         <#if manager_has_next>,</#if>
      </#list>
   ]
}
</#escape>
</#macro>
