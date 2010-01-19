<#escape x as jsonUtils.encodeJSONString(x)>
{
   "current": [<#list aspects.current as a>"${shortQName(a)}"<#if a_has_next>,</#if></#list>]
}
</#escape>