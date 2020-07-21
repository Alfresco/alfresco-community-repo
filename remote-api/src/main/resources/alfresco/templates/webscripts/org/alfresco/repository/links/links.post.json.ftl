<#escape x as jsonUtils.encodeJSONString(x)>
{
   "message": "${message}"<#if item??>,
   "name": "${item.name?string}"
</#if>
}
</#escape>