<#if success!false == true>
{
   "success": "true",
   "nodeRef": "${nodeRef!""}"
}
<#else>
{
   "success": "false",
   "error": "${msg(errorMessage!"", errorMessageArg!"")?html}"
}
</#if>