<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if error?exists>
   "error": "${error}"
<#elseif events?exists && events?size &gt; 0>
   <#assign prev = "">
   <#list events as item>
      <#assign event = item.event>
      <#assign date = event.start?string("M/d/yyyy")>
      <#if date != prev>
         <#assign counter = 0>
         <#if item_index &gt; 0>],</#if>
   "${date}": [
      </#if>
      <#if counter &gt; 0>,</#if>
   {
      "name": "${event.title}",
      "uri": "calendar/event/${siteId}/${event.systemName}",
      "startAt": {
          "iso8601": "${xmldate(event.start)}",
          "legacyDate": "${event.start?string("M/d/yyyy")}",
          "legacyTime": "${event.start?string("HH:mm")}",
      },
      "endAt": {
          "iso8601": "${xmldate(event.end)}",
          "legacyDate": "${event.end?string("M/d/yyyy")}",
          "legacyTime": "${event.end?string("HH:mm")}",
      },

      <#-- These are the old ones we'll get rid of soon -->
      "from": "${event.start?string("M/d/yyyy")}",
      "start": "${event.start?string("HH:mm")}",
      "to": "${event.end?string("M/d/yyyy")}",
      "end": "${event.end?string("HH:mm")}",

      "tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
   <#if event.recurrenceRule??>
      "recurrenceRule": "${event.recurrenceRule}",
   </#if>
   <#if event.lastRecurrence??>
      "recurrenceLastMeeting": "${event.lastRecurrence?string("M/d/yyyy")}",
   </#if>
      "ignoreEvents": [<#list item.ignoreEvents as ignoreEvent>"${ignoreEvent}"<#if ignoreEvent_has_next>,</#if></#list>]
   }
      <#assign counter = counter + 1>
      <#if !item_has_next>]</#if>
      <#assign prev = date>
   </#list>
</#if>
}
</#escape>
