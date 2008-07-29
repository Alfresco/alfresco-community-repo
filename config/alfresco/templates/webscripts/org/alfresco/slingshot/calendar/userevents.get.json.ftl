<#if !limit?exists><#assign limit = -1></#if>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if events?exists>
"events": [
<#list events as event>
	<#if event_index?string == limit?string><#break></#if>
	{
		"name" : "${event.name}",
		"title" : "${event.properties["ia:whatEvent"]}",
<#--	"description" : "${event.properties["ia:descriptionEvent"]}", -->
		"where" : "${event.properties["ia:whereEvent"]}",
		"start" : "${event.properties["ia:fromDate"]?string("dd MMMM yyyy")}",
		"end" : "${event.properties["ia:toDate"]?string("dd MMMM yyyy")}"
	}<#if event_has_next>,</#if>
</#list>
]
</#if>
}
</#escape>