<#macro personJSONinner person>
<#escape x as jsonUtils.encodeJSONString(x)>
	"url": "${url.serviceContext + "/api/person/" + person.properties.userName}",
	"userName": "${person.properties.userName}",
	"enabled": ${people.isAccountEnabled(person)?string("true","false")},
	<#if person.assocs["cm:avatar"]??>
	"avatar": "${"api/node/" + person.assocs["cm:avatar"][0].nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
	</#if>
	"firstName": <#if person.properties.firstName??>"${person.properties.firstName}"<#else>null</#if>,
	"lastName": <#if person.properties.lastName??>"${person.properties.lastName}"<#else>null</#if>,
	"jobtitle": <#if person.properties.jobtitle??>"${person.properties.jobtitle}"<#else>null</#if>,
	"organization": <#if person.properties.organization??>"${person.properties.organization}"<#else>null</#if>,
	"location": <#if person.properties.location??>"${person.properties.location}"<#else>null</#if>,
	"telephone": <#if person.properties.telephone??>"${person.properties.telephone}"<#else>null</#if>,
	"mobile": <#if person.properties.mobile??>"${person.properties.mobile}"<#else>null</#if>,
	"email": <#if person.properties.email??>"${person.properties.email}"<#else>null</#if>,
	"companyaddress1": <#if person.properties.companyaddress1??>"${person.properties.companyaddress1}"<#else>null</#if>,
	"companyaddress2": <#if person.properties.companyaddress2??>"${person.properties.companyaddress2}"<#else>null</#if>,
	"companyaddress3": <#if person.properties.companyaddress3??>"${person.properties.companyaddress3}"<#else>null</#if>,
	"companypostcode": <#if person.properties.companypostcode??>"${person.properties.companypostcode}"<#else>null</#if>,
	"companytelephone": <#if person.properties.companytelephone??>"${person.properties.companytelephone}"<#else>null</#if>,
	"companyfax": <#if person.properties.companyfax??>"${person.properties.companyfax}"<#else>null</#if>,
	"companyemail": <#if person.properties.companyemail??>"${person.properties.companyemail}"<#else>null</#if>,
	"skype": <#if person.properties.skype??>"${person.properties.skype}"<#else>null</#if>,
	"instantmsg": <#if person.properties.instantmsg??>"${person.properties.instantmsg}"<#else>null</#if>,
	"userStatus": <#if person.properties.userStatus??>"${person.properties.userStatus}"<#else>null</#if>,
	"userStatusTime": <#if person.properties.userStatusTime??>{ "iso8601": "${xmldate(person.properties.userStatusTime)}"}<#else>null</#if>,
	"googleusername": <#if person.properties.googleusername??>"${person.properties.googleusername}"<#else>null</#if>,
	"quota": <#if person.properties.sizeQuota??>${person.properties.sizeQuota?c}<#else>-1</#if>,
	"sizeCurrent": <#if person.properties.sizeCurrent??>${person.properties.sizeCurrent?c}<#else>0</#if>,
	"persondescription": <#if person.properties.persondescription??>"${stringUtils.stripUnsafeHTML(person.properties.persondescription.content)}"<#else>null</#if>
</#escape>
</#macro>

<#macro personJSON person>
{
<@personJSONinner person=person/>
}
</#macro>

<#macro personCapJSON person capabilities>
{
<@personJSONinner person=person/>,
	"capabilities":
	{
		<@serializeHash hash=capabilities/>
	}
}
</#macro>

<#macro personGroupsJSON person groups capabilities immutability>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<@personJSONinner person=person/>,
	"capabilities":
	{
		<@serializeHash hash=capabilities/>
	},
	"groups": [
	<#list groups as g>
		<#assign authName = g.properties["cm:authorityName"]>
		<#if authName?starts_with("GROUP_site")><#assign displayName = authName?substring(6)><#else><#assign displayName = g.properties["cm:authorityDisplayName"]!authName?substring(6)></#if>
	{
		"itemName": "${authName}",
		"displayName": "${displayName}"
	}<#if g_has_next>,</#if></#list>],
	"immutability":
	{
	   <@serializeHash hash=immutability/>
	}
}
</#escape>
</#macro>

<#macro personSummaryJSON person>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"url": "${url.serviceContext + "/api/person/" + person.properties.userName}",
	"userName": "${person.properties.userName}",
	"firstName": "${person.properties.firstName!""}",
	"lastName": "${person.properties.lastName!""}"
}
</#escape>
</#macro>

<#macro serializeHash hash>
<#escape x as jsonUtils.encodeJSONString(x)>
<#local first = true>
<#list hash?keys as key>
	<#if hash[key]??>
		<#local val = hash[key]>
		<#if !first>,<#else><#local first = false></#if>"${key}":
		<#if person.isTemplateContent(val)>"${val.content}"
		<#elseif person.isTemplateNodeRef(val)>"${val.nodeRef}"
		<#elseif val?is_date>"${val?datetime}"
		<#elseif val?is_boolean>${val?string}
		<#else>"${val}"
		</#if>
	</#if>
</#list>
</#escape>
</#macro>