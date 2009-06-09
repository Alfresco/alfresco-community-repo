<#escape x as jsonUtils.encodeJSONString(x)>
{
   "current": [<#list aspects.current as a>"${shortQName(a)}"<#if a_has_next>,</#if></#list>],
   "visible": [<#list aspects.visible as a>"${shortQName(a)}"<#if a_has_next>,</#if></#list>],
   "addable": [<#list aspects.addable as a>"${shortQName(a)}"<#if a_has_next>,</#if></#list>],
   "removeable": [<#list aspects.removeable as a>"${shortQName(a)}"<#if a_has_next>,</#if></#list>]
}
</#escape>