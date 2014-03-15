<#import "../generic-paged-results.lib.ftl" as gen/>

<#macro siteJSON item>
<#escape x as jsonUtils.encodeJSONString(x)>
	                "url" : "${url.serviceContext + "/api/sites/" + item.siteInfo.shortName}",
	                "sitePreset" : "${item.siteInfo.sitePreset}",
	                "shortName" : "${item.siteInfo.shortName}",
	                "title" : "${item.siteInfo.title}",
	                "description" : "${item.siteInfo.description}",
	                "createdDate" : "${xmldate(item.siteInfo.createdDate)}",
	                "lastModifiedDate" : "${xmldate(item.siteInfo.lastModifiedDate)}",
	                "visibility" : "${item.siteInfo.visibility}",
	                "userIsSiteManager" : "${item.currentUserSiteManager?c}",
	                "siteManagers" : [{
	                     <#list item.members as manager>
	                        "entry" : {
	                            "userName" : "${manager.userName}",
	                            "firstName" : "${manager.firstName}",
	                            "lastName" : "${manager.lastName}"
	                        }
	                    }<#if manager_has_next>, {</#if>
	                     </#list>
	                ]    
</#escape>
</#macro>

{
   <@gen.standardRestfulPagedResults data=data ; item>
          <@siteJSON item=item /> 
   </@gen.standardRestfulPagedResults>
}

