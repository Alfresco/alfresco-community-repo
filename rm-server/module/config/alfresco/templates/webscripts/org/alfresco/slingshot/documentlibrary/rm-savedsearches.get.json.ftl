<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
	<#list savedSearches as s>
		{
			"name": "${s.name}",
			"description": "${s.description!""}"
		}<#if s_has_next>,</#if>
	</#list>
	]
}
</#escape>