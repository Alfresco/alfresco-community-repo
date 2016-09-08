<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"url": "${nextaction.url}",
		"name": "${nextaction.name}",
		"label": "${nextaction.label}",
		"eventsEligible": ${nextaction.eventsEligible?string},
		<#if nextaction.asOf??>"asOf": "${nextaction.asOf}",</#if>
		<#if nextaction.startedAt??>"startedAt": "${nextaction.startedAt}",</#if>
		<#if nextaction.startedBy??>"startedBy": "${nextaction.startedBy}",</#if>
		<#if nextaction.startedByFirstName??>"startedByFirstName": "${nextaction.startedByFirstName}",</#if>
		<#if nextaction.startedByLastName??>"startedByLastName": "${nextaction.startedByLastName}",</#if>
		<#if nextaction.completedAt??>"completedAt": "${nextaction.completedAt}",</#if>
		<#if nextaction.completedBy??>"completedBy": "${nextaction.completedBy}",</#if>
		<#if nextaction.completedByFirstName??>"completedByFirstName": "${nextaction.completedByFirstName}",</#if>
		<#if nextaction.completedByLastName??>"completedByLastName": "${nextaction.completedByLastName}",</#if>
		"events": 
		[
			<#list nextaction.events as event>
			{
				"name": "${event.name}",
				"label": "${event.label}",
				"complete": ${event.complete?string},
				<#if event.completedAt??>"completedAt": "${event.completedAt}",</#if>
				<#if event.completedBy??>"completedBy": "${event.completedBy}",</#if>
				<#if event.completedByFirstName??>"completedByFirstName": "${event.completedByFirstName}",</#if>
				<#if event.completedByLastName??>"completedByLastName": "${event.completedByLastName}",</#if>
				"automatic": ${event.automatic?string}
			}<#if event_has_next>,</#if>
			</#list>
		]
	}
}
</#escape>