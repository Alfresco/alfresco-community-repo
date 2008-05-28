{
   "action":
   {
<#if action.error?exists>
      "error": "${action.error}"
<#else>
      "result":
      [
      <#list action.items as a>
         {
         }<#if a_has_next>,</#if>
      </#list>
      ]
</#if>
   }
}