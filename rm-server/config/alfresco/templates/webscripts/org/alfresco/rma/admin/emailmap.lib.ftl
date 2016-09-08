<#-- renders an email map object -->

<#macro emailmapJSON emailmap>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"mappings":
		[
			<#list emailmap as mapping> 
			{"from": "${mapping.from}", "to": "${mapping.to}" }<#if mapping_has_next>,</#if>
			</#list>
		]
	}
</#escape>
</#macro>