<#if events?exists>
<#assign dateFormat = "yyyyMMdd">
<#assign timeFormat = "HHmmss">
<#assign zoneFormat = "z">
BEGIN:VCALENDAR
PRODID:-//Alfresco Software//Calendar 1.0//EN
VERSION:2.0
<#list events as item>
<#assign event = item.event>

<#assign from = event.properties["ia:fromDate"]>
<#assign to = event.properties["ia:toDate"]>
BEGIN:VEVENT
UID:${event.id}
DTSTART;TZID=${from?string(zoneFormat)}:${from?string(dateFormat)}T${from?string(timeFormat)}
DTEND;TZID=${from?string(zoneFormat)}:${to?string(dateFormat)}T${to?string(timeFormat)}
SUMMARY:${event.properties["ia:whatEvent"]!""}
DTSTAMP;TZID=${event.properties["cm:created"]?string(zoneFormat)}:${event.properties["cm:created"]?string(dateFormat)}T${event.properties["cm:created"]?string(timeFormat)}
<#if event.properties["ia:descriptionEvent"]?exists>
DESCRIPTION:${event.properties["ia:descriptionEvent"]?replace("\\", "\\\\")?replace(",", "\\,")?replace(";", "\\;")?replace("\r?\n", "\\n", "r")}
</#if>
END:VEVENT
</#list>
END:VCALENDAR
<#else>
No events
</#if>
