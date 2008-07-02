<#macro personJSON person>
{
   "url" : "${url.serviceContext}/api/person/${person.properties.userName}",
   "userName" : "${person.properties.userName}",
   <#if person.properties.title??>
      "title" : "${person.properties.title}",
   <#else>
      "title" : undefined,
   </#if>
   <#if person.properties.firstName??>
      "firstName" : "${person.properties.firstName}",
   <#else>
      "firstName" : undefined,
   </#if>
   <#if person.properties.lastName??>
      "lastName" : "${person.properties.lastName}",
   <#else>
      "lastName" : undefined,
   </#if>
   <#if person.properties.organization??>
      "organisation" : "${person.properties.organization}",
   <#else>
      "organisation" : undefined,
   </#if>
   <#if person.properties.jobtitle??>
      "jobTitle" : "${person.properties.jobtitle}",
   <#else>
      "jobTitle" : undefined,
   </#if>
   <#if person.properties.email??>
      "email" : "${person.properties.email}"
   <#else>
      "email" : undefined
   </#if>
}
</#macro>

<#macro personSummaryJSON person>
{
   "url" : "${url.serviceContext}/api/person/${person.properties.userName}",
   "userName" : "${person.properties.userName}",
   "firstName" : "${person.properties.firstName}",
   "lastName" : "${person.properties.lastName}"
}
</#macro>