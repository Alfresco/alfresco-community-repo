<#if events?exists>
<#setting time_zone="GMT">
<#assign dateFormat = "yyyyMMdd">
<#assign timeFormat = "HHmmss">
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Alfresco Software//Calendar 1.1//EN
<#list events as item>
<#assign event = item.event>
<#assign from = event.properties["ia:fromDate"]>
<#assign to = event.properties["ia:toDate"]>
<#assign created = event.properties["cm:created"]>
BEGIN:VEVENT
UID:${event.id}
DTSTART:${from?string(dateFormat)}T${from?string(timeFormat)}Z
DTEND:${to?string(dateFormat)}T${to?string(timeFormat)}Z
SUMMARY:${event.properties["ia:whatEvent"]!""}
DTSTAMP:${created?string(dateFormat)}T${created?string(timeFormat)}Z
<#if event.properties["ia:descriptionEvent"]?exists>
DESCRIPTION:${event.properties["ia:descriptionEvent"]?replace("\\", "\\\\")?replace(",", "\\,")?replace(";", "\\;")?replace("\r?\n", "\\n", "r")}
</#if>
END:VEVENT
</#list>
END:VCALENDAR
<#else>
No events
</#if>