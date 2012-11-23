<#macro personJSONinner person>
<#local p=person.properties>
<#escape x as jsonUtils.encodeJSONString(x)>
	"url": "${url.serviceContext + "/api/person/" + p.userName}",
	"userName": "${p.userName}",
	"enabled": ${people.isAccountEnabled(person)?string("true","false")},
	<#if person.assocs["cm:avatar"]??>
	"avatar": "${"api/node/" + person.assocs["cm:avatar"][0].nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
	</#if>
	"firstName": <#if p.firstName??>"${p.firstName}"<#else>null</#if>,
	"lastName": <#if p.lastName??>"${p.lastName}"<#else>null</#if>,
	"jobtitle": <#if p.jobtitle??>"${p.jobtitle}"<#else>null</#if>,
	"organization": <#if p.organization??>"${p.organization}"<#else>null</#if>,
	"organizationId": <#if p.organizationId??>"${p.organizationId}"<#else>null</#if>, 
	"location": <#if p.location??>"${p.location}"<#else>null</#if>,
	"telephone": <#if p.telephone??>"${p.telephone}"<#else>null</#if>,
	"mobile": <#if p.mobile??>"${p.mobile}"<#else>null</#if>,
	"email": <#if p.email??>"${p.email}"<#else>null</#if>,
	"companyaddress1": <#if p.companyaddress1??>"${p.companyaddress1}"<#else>null</#if>,
	"companyaddress2": <#if p.companyaddress2??>"${p.companyaddress2}"<#else>null</#if>,
	"companyaddress3": <#if p.companyaddress3??>"${p.companyaddress3}"<#else>null</#if>,
	"companypostcode": <#if p.companypostcode??>"${p.companypostcode}"<#else>null</#if>,
	"companytelephone": <#if p.companytelephone??>"${p.companytelephone}"<#else>null</#if>,
	"companyfax": <#if p.companyfax??>"${p.companyfax}"<#else>null</#if>,
	"companyemail": <#if p.companyemail??>"${p.companyemail}"<#else>null</#if>,
	"skype": <#if p.skype??>"${p.skype}"<#else>null</#if>,
	"instantmsg": <#if p.instantmsg??>"${p.instantmsg}"<#else>null</#if>,
	"userStatus": <#if p.userStatus??>"${p.userStatus}"<#else>null</#if>,
	"userStatusTime": <#if p.userStatusTime??>{ "iso8601": "${xmldate(p.userStatusTime)}"}<#else>null</#if>,
	"googleusername": <#if p.googleusername??>"${p.googleusername}"<#else>null</#if>,
	"quota": <#if p.sizeQuota??>${p.sizeQuota?c}<#else>-1</#if>,
	"sizeCurrent": <#if p.sizeCurrent??>${p.sizeCurrent?c}<#else>0</#if>,
	"emailFeedDisabled": <#if p.emailFeedDisabled??>${p.emailFeedDisabled?string("true","false")}<#else>false</#if>,
	"persondescription": <#if p.persondescription??>"${stringUtils.stripUnsafeHTML(p.persondescription.content)}"<#else>null</#if>
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