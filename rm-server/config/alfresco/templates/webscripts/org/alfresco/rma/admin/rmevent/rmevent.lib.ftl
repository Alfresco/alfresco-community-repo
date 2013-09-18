<#-- renders an rm event object -->

<#macro eventJSON event>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "eventName": "${event.name}",
    "eventDisplayLabel": "${msg(event.displayLabel)}",             
    "eventType":"${event.type}"
}
</#escape>
</#macro>

