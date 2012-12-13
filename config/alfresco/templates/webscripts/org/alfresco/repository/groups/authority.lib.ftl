<#-- renders an authority object which can be either a GROUP or USER (and possibly ROLE in future)-->
<#macro authorityJSON authority>
<#escape x as jsonUtils.encodeJSONString(x)>
{
		   "authorityType": "${authority.authorityType}",
		   "shortName": "${authority.shortName}",
		   "fullName": "${authority.fullName}",
		   "displayName": "${authority.displayName}",
		   <#if authority.authorityType = "GROUP">
		   "url": "/api/groups/${authority.shortName?url}"
		   </#if>
		   <#if authority.authorityType = "USER">
		   "url": "/api/people/${authority.shortName?url}"
		   </#if>
           <#if authority.zones?exists>
           ,"zones":
           [
           <#list authority.zones as zone>
              "${zone}"<#if zone_has_next>,</#if>
           </#list>
           ]
           </#if>
}
</#escape>
</#macro>

<#-- Renders paging objects. -->
<#import "../generic-paged-results.lib.ftl" as genericPaging />
