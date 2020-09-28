<#-- Shows the translations applied to a doc through the translatable aspect -->
<b>${message("templates.translatable.translatable")}</b>
<#if document?exists>
   <#if hasAspect(document, "cm:translatable") = 1>
      ${message("templates.translatable.yes")}<br>
      <table>
      <#if document.assocs["cm:translations"]?exists>
         <#list document.assocs["cm:translations"] as t>
            <tr><td>${t.content}</td></tr>
         </#list>
      </#if>
      </table>
   <#else>
      ${message("templates.translatable.no")}<br>
   </#if>
<#else>
   ${message("templates.translatable.no_document_found")}<br>
</#if>
