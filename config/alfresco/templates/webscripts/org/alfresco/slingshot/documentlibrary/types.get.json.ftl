<#escape x as jsonUtils.encodeJSONString(x)>
{
   "current": "${currentType}",
   "selectable": [<#list selectableTypes as t>"${shortQName(t)}"<#if t_has_next>,</#if></#list>]
}
</#escape>