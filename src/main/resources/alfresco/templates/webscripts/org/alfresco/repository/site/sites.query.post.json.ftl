<#import "site.lib.ftl" as siteLib/>

[
   <#list sites?sort_by("shortName") as site>
      <#if isShortNameMode>
         <@siteLib.siteJSONManagers site=site roles="user"/>
      <#else>
         <@siteLib.siteJSON site=site/>
      </#if>
      <#if site_has_next>,</#if>
   </#list>
]