{
	"data" : 
	{
		"capabilities" :
		[
		<#list capabilities as capability>
			"${capability}"<#if capability_has_next>,</#if>
		</#list>
		]
	}
}