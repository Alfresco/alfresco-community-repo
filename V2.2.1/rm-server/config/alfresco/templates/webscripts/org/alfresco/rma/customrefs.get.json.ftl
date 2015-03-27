<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"nodeName": "${nodeName!""}",
		"nodeTitle": "${nodeTitle!""}",
		"customReferencesFrom":
		[
			<#list customRefsFrom as ref>
			{
				<#assign keys = ref?keys>
				<#list keys as key>"${key}": "${ref[key]}"<#if key_has_next>,</#if></#list>
			}<#if ref_has_next>,</#if>
			</#list>
		],
		"customReferencesTo":
		[
			<#list customRefsTo as ref>
			{
				<#assign keys = ref?keys>
				<#list keys as key>"${key}": "${ref[key]}"<#if key_has_next>,</#if></#list>
			}<#if ref_has_next>,</#if>
			</#list>
		]
	}
}
</#escape>
