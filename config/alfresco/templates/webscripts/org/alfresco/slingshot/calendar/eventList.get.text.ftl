<#if events?exists>
<#assign dateFormat = "yyyyMMdd">
<#assign timeFormat = "HHmmss">
BEGIN:VCALENDAR
PRODID:-//Alfresco Software//Calendar 1.0//EN
VERSION:2.0
<#list events as event>
<#assign from = event.properties["ia:fromDate"]>
<#assign to = event.properties["ia:toDate"]>
BEGIN:VEVENT
UID:${event.id}
DTSTART:${from?string(dateFormat)}T${from?string(timeFormat)}Z
DTEND:${to?string(dateFormat)}T${to?string(timeFormat)}Z
SUMMARY:${event.properties["ia:whatEvent"]!""}
DTSTAMP:${event.properties["cm:created"]?string(dateFormat)}T${event.properties["cm:created"]?string(timeFormat)}Z
<#if event.properties["ia:descriptionEvent"]?exists>
DESCRIPTION:${event.properties["ia:descriptionEvent"]?replace("\\", "\\\\")?replace(",", "\\,")?replace(";", "\\;")?replace("\r?\n", "\\n", "r")}
</#if>
END:VEVENT
</#list>
END:VCALENDAR
<#else>
No events
</#if>
