<#macro listsJSON lists>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"dispositionActions":
		{
			"url": "${lists.dispositionActions.url}",
			"items":
			[
				<#list lists.dispositionActions.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"events":
		{
			"url": "${lists.events.url}",
			"items":
			[
				<#list lists.events.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}",
					"automatic": ${item.automatic?string}
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"periodTypes":
		{
			"url": "${lists.periodTypes.url}",
			"items":
			[
				<#list lists.periodTypes.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"periodProperties":
		{
			"url": "${lists.periodProperties.url}",
			"items":
			[
				<#list lists.periodProperties.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"auditEvents":
		{
			"url": "${lists.auditEvents.url}",
			"items":
			[
				<#list lists.auditEvents.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		}
	}
}
</#escape>
</#macro>
