{
<#if result.error?exists>
  "error" : "${result.error}"
<#else>
  "pagetext" : "${result.pagetext}"
</#if>
}