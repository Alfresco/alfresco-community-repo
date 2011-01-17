<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if events?exists && events?size &gt; 0>
   <#assign prev = "">
   <#-- We do the sort here as the sort in the JavaScript doesn't seem to work as expected! -->
   <#list events?sort_by(["fromDate"]) as item>
      <#assign event = item.event>
      <#assign date = event.properties["ia:fromDate"]?string("M/d/yyyy")>
      <#if date != prev>
         <#assign counter = 0>
         <#if item_index &gt; 0>],</#if>
   "${date}": [
      </#if>
      <#if counter &gt; 0>,</#if>
   {
      "name": "${event.properties["ia:whatEvent"]}",
      "from": "${event.properties["ia:fromDate"]?string("M/d/yyyy")}",
      "start": "${event.properties["ia:fromDate"]?string("HH:mm")}",
      "to": "${event.properties["ia:toDate"]?string("M/d/yyyy")}",
      "end": "${event.properties["ia:toDate"]?string("HH:mm")}",
      "uri": "calendar/event/${siteId}/${event.name}",
      "tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
   <#if event.properties["ia:recurrenceRule"]??>
      "recurrenceRule": "${event.properties["ia:recurrenceRule"]}",
   </#if>
   <#if event.properties["ia:recurrenceLastMeeting"]??>
      "recurrenceLastMeeting": "${event.properties["ia:recurrenceLastMeeting"]?string("M/d/yyyy")}",
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
