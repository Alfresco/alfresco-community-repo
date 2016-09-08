<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
	<#list savedSearches as s>
		{
			"name": "${s.name}",
			"description": "${s.description!""}",
			"query": "${s.query}",
			"params": "${s.params}",
			"sort": "${s.sort}"
		}<#if s_has_next>,</#if>
	</#list>
	]
}
</#escape>