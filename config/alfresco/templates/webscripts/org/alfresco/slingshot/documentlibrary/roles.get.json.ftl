<#escape x as jsonUtils.encodeJSONString(x)>
{
   "roles":
   [
   <#list roles as role>
      "${role}"<#if role_has_next>,</#if>
   </#list>
   ]
}
</#escape>
