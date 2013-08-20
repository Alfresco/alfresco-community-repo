<#if events?exists>
<#setting time_zone="GMT">
<#assign dateFormat = "yyyyMMdd">
<#assign timeFormat = "HHmmss">
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Alfresco Software//Calendar 1.1//EN
<#list events as item>
<#assign event = item.event>
<#assign from = event.start>
<#assign to = event.end>
<#assign created = event.createdAt>
BEGIN:VEVENT
UID:${event.nodeRef.id}
<#if item.allDayEnd?exists>
DTSTART;VALUE=DATE:${from?string(dateFormat)}
DTEND;VALUE=DATE:${item.allDayEnd?string(dateFormat)}
<#else>
DTSTART:${from?string(dateFormat)}T${from?string(timeFormat)}Z
DTEND:${to?string(dateFormat)}T${to?string(timeFormat)}Z
</#if>
SUMMARY:${event.title!""}
DTSTAMP:${created?string(dateFormat)}T${created?string(timeFormat)}Z
<#if event.recurrenceRule??>
RRULE:${event.recurrenceRule}
</#if>
<#if event.description?exists>
DESCRIPTION:${event.description?replace("\\", "\\\\")?replace(",", "\\,")?replace(";", "\\;")?replace("\r?\n", "\\n", "r")}
</#if>
END:VEVENT
</#list>
END:VCALENDAR
<#else>
No events
</#if>