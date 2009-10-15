<#if !limit?exists><#assign limit = -1></#if>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if events?exists>
"events": [
<#list events?sort_by("when") as event>
	<#if event_index?string == limit?string><#break></#if>
	{
		"name": "${event.name?html}",
		"title": "${event.title?html}",
		"where": "${event.where?html}",
		"when": "${xmldate(event.when)}",
		"url": "page/site/${event.site}/calendar?date=${event.when?string("yyyy-MM-dd")}",
		"start": "${event.start?string("HH:mm")}",
		"end": "${event.end?string("HH:mm")}",
      "endDate" : "${xmldate(event.end)}",
		"site": "${event.site?html}",
		"siteTitle": "${event.siteTitle?html}",
		"allday": "${event.allday}",
      "tags": "${event.tags?html}",
      "duration": "${event.duration?html}"
	}<#if event_has_next>,</#if>
</#list>
]
</#if>
}
</#escape>