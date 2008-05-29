<#if events?exists>
<#assign dateFormat = "yyyyMMdd">
<#assign timeFormat = "HHmmss">
BEGIN:VCALENDAR
VERSION:2.0
<#list events as event>
<#assign from = event.properties["ia:fromDate"]>
<#assign to = event.properties["ia:toDate"]>
BEGIN:VEVENT
DTSTART:${from?string(dateFormat)}T${from?string(timeFormat)}Z
DTEND:${to?string(dateFormat)}T${to?string(timeFormat)}Z
SUMMARY:${event.properties["ia:whatEvent"]!""}
DESCRIPTION:${event.properties["ia:descriptionEvent"]!""}
END:VEVENT
</#list>
END:VCALENDAR
</#if>
