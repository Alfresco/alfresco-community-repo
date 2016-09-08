<#-- renders an rm role object -->
<#macro roleJSON role>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"name": "${role.name}",
	"displayLabel": "${role.displayLabel}",
	"capabilities" :
	[
	<#list role.capabilities as capability>
		"${capability}"<#if capability_has_next>,</#if>
	</#list>
	]
}
</#escape>
</#macro>