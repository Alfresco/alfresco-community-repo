<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
	"error" : "${result.error}"
<#else>
	"title" : "${result.title}"
</#if>
}
</#escape>
