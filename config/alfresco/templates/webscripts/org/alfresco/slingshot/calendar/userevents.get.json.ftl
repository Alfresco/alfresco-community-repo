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
            "url": "page/site/${event.siteName}/calendar?date=${event.legacyDateFrom}",

            "startAt": {
                "iso8601": "${event.start}",
                "legacyTime": "${event.legacyTimeFrom}"
            },
            "endAt": {
                "iso8601": "${event.end}",
                "legacyTime": "${event.legacyTimeTo}"
            },

            "site": "${event.siteName}",
            "siteTitle": "${event.siteTitle}",
            "allday": "${event.allday?string}",
            "tags": [<#list event.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
            "duration": "${event.duration}",
            "isoutlook": "${event.isoutlook?string}",
			
            "permissions":
            {
                "isEdit": ${event.canEdit?string},
                "isDelete": ${event.canDelete?string}
            }
         }<#if event_has_next>,</#if>
      </#list>
      ]
   </#if>
}
</#escape>
