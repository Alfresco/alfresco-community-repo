<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"dodCustomTypes":
		[
			<#list dodCustomTypes as aspDef>
			{
				"name": "${aspDef.name.prefixString}",
				"title": "${aspDef.title!""}"
			}<#if aspDef_has_next>,</#if>
			</#list>
		]
	}
}
</#escape>