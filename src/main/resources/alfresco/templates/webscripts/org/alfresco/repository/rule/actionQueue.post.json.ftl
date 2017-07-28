
{
	"data" : 
	{
		"status" : "${actionExecStatus}",
		"actionedUponNode" : "${actionedUponNode?string}",
		<#if exception??>
		"exception" : 
		{
			"message" : "${exception.message}",
			"stackTrace" : 			
			[
			<#list exception.stackTrace as stackTraceElement>
			"${stackTraceElement?string}"<#if stackTraceElement_has_next>,</#if>
			</#list>
			]			
		},
		</#if>
		"action" : 
		${action?string}
	}
}