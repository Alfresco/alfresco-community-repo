<#macro personJSON person>
<#escape x as jsonUtils.encodeJSONString(x)>
{	
   "url" : "${url.serviceContext + "/api/person/" + person.properties.userName}",
   "userName" : "${person.properties.userName}",
   <#if person.assocs["cm:avatar"]??>
   "avatar" : "${"api/node/" + person.assocs["cm:avatar"][0].nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
   </#if>
   <#if person.properties.firstName??>
   "firstName" : "${person.properties.firstName}",
   <#else>
   "firstName" : null,
   </#if>
   <#if person.properties.lastName??>
   "lastName" : "${person.properties.lastName}",
   <#else>
   "lastName" : null,
   </#if>
   <#if person.properties.title??>
   "title" : "${person.properties.title}",
   <#else>
   "title" : null,
   </#if>
   <#if person.properties.organization??>
   "organisation" : "${person.properties.organization}",
   <#else>
   "organisation" : null,
   </#if>
   <#if person.properties.jobtitle??>
   "jobtitle" : "${person.properties.jobtitle}",
   <#else>
   "jobtitle" : null,
   </#if>
   <#if person.properties.email??>
   "email" : "${person.properties.email}"
   <#else>
   "email" : null
   </#if>
}
</#escape>
</#macro>

<#macro personSummaryJSON person>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "url" : "${url.serviceContext + "/api/person/" + person.properties.userName}",
   "userName" : "${person.properties.userName}",
   "firstName" : "${person.properties.firstName}",
   "lastName" : "${person.properties.lastName}"
}
</#escape>
</#macro>