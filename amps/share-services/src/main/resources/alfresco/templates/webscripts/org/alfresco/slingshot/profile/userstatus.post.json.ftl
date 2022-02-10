<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if success>
   "userStatus": "${userStatus}",
   "userStatusTime": { "iso8601": "${xmldate(userStatusTime)}"},
</#if>
   "success": ${success?string}
}
</#escape>