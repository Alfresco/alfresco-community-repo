<#macro resultsJSON results>
{
   "results":
   [
   <#list results as r>
      {
         "id": "${r.id!""}",
         "action": "${r.action}",
         "success": ${r.success?string}
      }<#if r_has_next>,</#if>
   </#list>
   ]
}
</#macro>