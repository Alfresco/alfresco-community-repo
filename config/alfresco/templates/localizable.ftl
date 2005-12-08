<#-- Shows if a document is localizable and the locale if set -->
<b>Localisable:</b>
<#if hasAspect(document, "cm:localizable") = 1>
   Yes<br>
   <#if document.properties.locale?exists>
      Locale: ${document.properties.locale.properties.name}
   </#if>
<#else>
   No<br>
</#if>
