<#macro actionJSON action>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id": "${action.id}",
		"url": "${action.url}",
		"index": ${action.index},
		"name": "${action.name}",
		"label": "${action.label}",
		<#if (action.name == "destroy") && action.ghostOnDestroy??>"ghostOnDestroy": "${action.ghostOnDestroy}",</#if>
		<#if action.description??>"description": "${action.description}",</#if>
		<#if action.period??>"period": "${action.period}",</#if>
		<#if action.periodProperty??>"periodProperty": "${action.periodProperty}",</#if>
		<#if action.location??>"location": "${action.location}",</#if>
		<#if action.events??>"events": [<#list action.events as event>"${event}"<#if event_has_next>,</#if></#list>],</#if>
		"eligibleOnFirstCompleteEvent": ${action.eligibleOnFirstCompleteEvent?string}
	}
</#escape>
</#macro>