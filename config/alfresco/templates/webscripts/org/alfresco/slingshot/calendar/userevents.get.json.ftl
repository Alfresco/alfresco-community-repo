<#if !limit?exists><#assign limit = -1></#if>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if events?exists>
"events": [
<#list events?sort_by(["properties", "ia:fromDate"]) as event>
	<#if event_index?string == limit?string><#break></#if>
	{
		"name" : "${event.name}",
		"title" : "${event.properties["ia:whatEvent"]}",
		"where" : "${event.properties["ia:whereEvent"]}",
		"when" : "${event.properties["ia:fromDate"]?string("dd MMMM yyyy")}",
	   "url" : "page/site/${event.parent.parent.name}/calendar?date=${event.properties["ia:fromDate"]?string("MM/dd/yyyy")}",
		"start" : "${event.properties["ia:fromDate"]?string("HH:mm")}",
		"end" : "${event.properties["ia:toDate"]?string("HH:mm")}",
		"site" : "${event.parent.parent.name}"
	}<#if event_has_next>,</#if>
</#list>
]
</#if>
}
</#escape>