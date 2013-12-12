<#import "rmevent.lib.ftl" as rmEventLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		<#list events as event>
		"${event.name}":
		<@rmEventLib.eventJSON event=event /><#if event_has_next>,</#if>
		</#list>
	}
}
</#escape>