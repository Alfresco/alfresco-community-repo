<#if !limit?exists><#assign limit = -1></#if>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if events?exists>
      "events": [
      <#list events as event>
         <#if event_index?string == limit?string><#break></#if>
         {
            "name": "${event.name}",
            "title": "${event.title}",
            "where": "${event.where}",
            "description": "${event.description}",
            "url": "page/site/${event.siteName}/calendar?date=${event.start?string("yyyy-MM-dd")}",

            "startAt": {
                "iso8601": "${xmldate(event.start)}",
                "legacyTime": "${event.start?string("HH:mm")}"
            },
            "endAt": {
                "iso8601": "${xmldate(event.end)}",
                "legacyTime": "${event.end?string("HH:mm")}"
            },
            "when": "${xmldate(event.start)}",
            "endDate" : "${xmldate(event.end)}",
            "start": "${event.start?string("HH:mm")}",
            "end": "${event.end?string("HH:mm")}",

            "site": "${event.siteName}",
            "siteTitle": "${event.siteTitle}",
            "allday": "${event.allday?string}",
            "tags": [<#list event.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
            "duration": "${event.duration}",
            "isoutlook": "${event.isoutlook?string}"
         }<#if event_has_next>,</#if>
      </#list>
      ]
   </#if>
}
</#escape>
