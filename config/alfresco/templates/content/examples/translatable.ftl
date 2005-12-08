<#-- Shows the translations applied to a doc through the translatable aspect -->
<b>Translatable:</b>
<#if hasAspect(document, "cm:translatable") = 1>
   Yes<br>
   <table>
   <#list document.assocs["cm:translations"] as t>
      <tr><td>${t.content}</td></tr>
   </#list>
   </table>
<#else>
   No<br>
</#if>
