<#macro personSummaryJSON person>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"url": "${url.serviceContext + "/api/people/" + person.properties.userName}",
	"userName": "${person.properties.userName}",
	"firstName": "${person.properties.firstName!""}",
	"lastName": "${person.properties.lastName!""}",
	"jobtitle": "${person.properties.jobtitle!""}",
	"location": "${person.properties.location!""}",
	"email": "${person.properties.email!""}",
	"organization": "${person.properties.organization!""}"
}
</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${data.totalRecords?c},
	"startIndex": ${data.startIndex?c},
	"items":
	[
		<#list data.items as person>
		<@personSummaryJSON person=person/><#if person_has_next>,</#if>
		</#list>
	]
}
</#escape>