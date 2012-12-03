<#-- renders an rm role object -->
<#macro roleJSON role>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${role.name}",
   "displayLabel": "${role.displayLabel}",
   "capabilities":
   {
   <#list role.capabilities?keys as capability>
      "${capability}": "${role.capabilities[capability]}" <#if capability_has_next>,</#if>
   </#list>
   }
}
</#escape>
</#macro>