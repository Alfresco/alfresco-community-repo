<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
	"error" : "${result.error}"
<#else>
	"name" : "${result.name}"
</#if>
}
</#escape>
