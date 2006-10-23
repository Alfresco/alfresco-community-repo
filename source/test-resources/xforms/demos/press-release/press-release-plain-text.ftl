<#ftl ns_prefixes={"alfresco", "http://www.alfresco.org/alfresco"}>
--- ${.vars["/alfresco:press_release/alfresco:title"]} ---

-- ${.vars["/alfresco:press_release/alfresco:abstract"]} --
<#list .vars["/alfresco:press_release/alfresco:body"] as body>
  <#if body_index = 0>
${.vars["/alfresco:press_release/alfresco:location"]}--${.vars["/alfresco:press_release/alfresco:launch_date"]}--
  </#if>
${body?trim}
</#list>
<#list .vars["/alfresco:press_release/alfresco:include_company_footer"] as cf>
<#assign cf_document=alfresco.getXMLDocument(cf)>
-- About ${cf_document["/alfresco:company_footer/alfresco:name"]} --
  <#list cf_document["/alfresco:company_footer/alfresco:body"] as body>
${body?trim}
  </#list>
</#list>
<#if .vars["/alfresco:press_release/alfresco:include_media_contacts"] = "true">
-- Media Contacts --
John Newton
Alfresco Software Inc.
+44 1628 860639
press@alfresco.com

Chuck Tanowitz
Schwartz Communications
+1 781 684-0770
alfresco@schwartz-pr.com
</#if>
