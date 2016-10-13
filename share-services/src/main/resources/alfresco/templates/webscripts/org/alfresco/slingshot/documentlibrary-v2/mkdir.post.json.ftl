<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRefs": [
      <#list nodeRefs as r>"${r}"<#if r_has_next>,</#if></#list>
   ]
}
</#escape>