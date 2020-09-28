<#-- Shows if a document is localizable and the locale if set -->
<b>${message("templates.localizable.localisable")}</b>
<#if document?exists>
   <#if hasAspect(document, "cm:localizable") = 1>
      ${message("templates.localizable.yes")}<br>
      <#if document.properties.locale?exists>
         ${message("templates.localizable.locale")} ${document.properties.locale.properties.name}
      </#if>
   <#else>
      ${message("templates.localizable.no")}<br>
   </#if>
<#else>
   ${message("templates.localizable.no_document_found")}
</#if>