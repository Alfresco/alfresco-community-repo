<#-- Web Site Membership renders an authority object which can be either a Java Script person or a Java Script group -->
<#macro membershipJSON site role authority>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"role": "${role}",
<#if authority.authorityType?? && authority.authorityType = "GROUP">
<#-- this is a group authority type -->
	"authority":
	{
		"authorityType": "${authority.authorityType}",
		"shortName": "${authority.shortName}",
		"fullName": "${authority.fullName!""}",
		"displayName": "${authority.displayName}",
		"url": "${url.serviceContext + "/api/groups/" + authority.shortName}"
	},
	"url": "${url.serviceContext + "/api/sites/" + site.shortName + "/memberships/" + authority.fullName!""}"
<#else>
<#-- this is a person authority type -->
<#-- nested directive is used to add custom info about a person.e.g. if he belongs to a group? -->
   <#nested>
	"authority":
	{
		"authorityType": "USER",
		"fullName": "${authority.properties.userName}",
		"userName": "${authority.properties.userName}",
		"firstName": "${authority.properties.firstName!""}",
		"lastName": "${authority.properties.lastName!""}",
	<#if authority.assocs["cm:avatar"]??>
	   <#assign avatarNodeRef=authority.assocs["cm:avatar"][0].nodeRef?string/>
		"avatar": "api/node/${avatarNodeRef?replace('://','/')}/content/thumbnails/avatar",
		"avatarNode": "${avatarNodeRef}",
	</#if>
	<#if authority.properties.jobtitle??>
		"jobtitle": "${authority.properties.jobtitle}",
	</#if>
	<#if authority.properties.organization??>
		"organization": "${authority.properties.organization}",
	</#if>
	<#if authority.properties.userStatus??>
		"userStatus": "${authority.properties.userStatus}",
		"userStatusTime": { "iso8601": "${xmldate(authority.properties.userStatusTime)}"},
	</#if>
		"url": "${url.serviceContext + "/api/people/" + authority.properties.userName}"
	},
	"url": "${url.serviceContext + "/api/sites/" + site.shortName + "/memberships/" + authority.properties.userName}"
</#if>
}
</#escape>
</#macro>