<#escape x as jsonUtils.encodeJSONString(x)>
{
 "linkNodes" : 
 [
	<#list results as result>
	{
	 	"nodeRef" : "${result.nodeRef}"
	}
		<#if result_has_next>,</#if>
	</#list>
 ],
 "successCount": "${successCount}",
 "failureCount": "${failureCount}",
 "overallSuccess": "${overallSuccess?c}"
}
</#escape>