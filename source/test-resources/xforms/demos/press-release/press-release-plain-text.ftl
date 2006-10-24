<#ftl ns_prefixes={"D", "http://www.alfresco.org/alfresco/pr"}>

<#macro show_heading heading>
${heading}
<#list 1..heading?length as index>-</#list>
</#macro>

<@show_heading heading="Title: ${press_release.title}"/>

<@show_heading heading="Abstract: ${press_release.abstract}"/>

<#list press_release.body as body>
  <#if body_index = 0>
${press_release.location}--${press_release.launch_date}--
  </#if>
${body?trim}
</#list>
<#list press_release.include_company_footer as cf>
<#assign cf_document=alfresco.parseXMLDocument(cf)>

<@show_heading heading="About ${cf_document.name}"/>
  <#list cf_document.body as body>
${body?trim}
  </#list>
</#list>
<#if press_release.include_media_contacts = "true">

<@show_heading heading="Media Contacts"/>
John Newton
Alfresco Software Inc.
+44 1628 860639
press@alfresco.com

Chuck Tanowitz
Schwartz Communications
+1 781 684-0770
alfresco@schwartz-pr.com
</#if>
