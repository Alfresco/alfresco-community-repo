<#if !limit?exists><#assign limit = -1></#if>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if events?exists>
"events": [
<#list events?sort_by("when") as event>
	<#if event_index?string == limit?string><#break></#if>
	{
		"name": "${event.name}",
		"title": "${event.title}",
		"where": "${event.where}",
		"when": "${xmldate(event.when)}",
	    "url": "page/site/${event.site}/calendar?date=${event.when?string("yyyy-MM-dd")}",
		"start": "${event.start?string("HH:mm")}",
		"end": "${event.end?string("HH:mm")}",
		"site": "${event.site}",
		"siteTitle": "${event.siteTitle}",
		"allday": "${event.allday}",
      "tags": "${event.tags}"
	}<#if event_has_next>,</#if>
</#list>
]
</#if>
}
</#escape>