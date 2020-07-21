<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list thumbnailDefinitions as def>
		"${def}"
		<#if def_has_next>,</#if>
	</#list>
]
</#escape>