{
<#if events?exists && events?size &gt; 0>
<#assign prev = "">
<#list events as event>
<#assign date = event.properties["ia:fromDate"]?string("MM/dd/yyyy")>
<#if date != prev>
<#assign counter = 0>
<#if event_index &gt; 0>],</#if>
"${date}" : [
</#if>
<#if counter &gt; 0>,</#if>
{
  "name" : "${event.properties["ia:whatEvent"]}"
}
<#assign counter = counter + 1>
<#if !event_has_next>]</#if>
<#assign prev = date>
</#list>
</#if>
}
