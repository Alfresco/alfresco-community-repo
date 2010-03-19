<#macro resultsJSON results>
   <#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalResults": ${results?size},
   "overallSuccess": ${overallSuccess?string},
   "successCount": ${successCount},
   "failureCount": ${failureCount},
   "results":
   [
      <#list results as r>
      {
         <#list r?keys as key>
            <#assign value = r[key]>
            <#if value?is_number || value?is_boolean>
         "${key}": ${value?string}<#if key_has_next>,</#if>
            <#else>
         "${key}": "${value}"<#if key_has_next>,</#if>
            </#if>
            </#list>
      }<#if r_has_next>,</#if>
      </#list>
   ]
}
   </#escape>
</#macro>