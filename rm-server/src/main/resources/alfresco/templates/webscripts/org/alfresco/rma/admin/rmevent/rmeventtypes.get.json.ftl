<#import "rmevent.lib.ftl" as rmEventLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		<#list eventtypes as eventtype>
		"${eventtype.name}":
		{
		  "eventTypeName" : "${eventtype.name}",
		  "eventTypeDisplayLabel" : "${eventtype.displayLabel}" 
		}<#if eventtype_has_next>,</#if>
		</#list>
	}
}
</#escape>