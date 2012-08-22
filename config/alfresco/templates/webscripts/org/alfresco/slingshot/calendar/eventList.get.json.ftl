<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if error?exists>
   "error": "${error}"
<#elseif events?exists && events?size &gt; 0>
   <#assign prev = "">
   <#list events as item>
      <#-- Note - use item not event start for repeating events expansion -->
      <#assign date = item.legacyDateFrom>
      <#assign event = item.event>
      <#if date != prev>
         <#assign counter = 0>
         <#if item_index &gt; 0>],</#if>
   "${date}": [
      </#if>
      <#if counter &gt; 0>,</#if>
   {
      "name": "${item.title}",
      "uri": "calendar/event/${siteId}/${event.systemName}",
      "startAt": {
          "iso8601": "${item.start}",
          "legacyDate": "${item.legacyDateFrom}",
          "legacyTime": "${item.legacyTimeFrom}"
      },
      "endAt": {
          "iso8601": "${item.end}",
          "legacyDate": "${item.legacyDateTo}",
          "legacyTime": "${item.legacyTimeTo}"
      },

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
