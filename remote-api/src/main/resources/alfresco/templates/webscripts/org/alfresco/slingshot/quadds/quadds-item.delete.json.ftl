<#if success!false == true>
{
   "success": "true",
   "nodeRef": "${nodeRef!""}"
}
<#else>
{
   "success": "false",
   "error": "${errorMessage!""?html}"
}
</#if>