<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if (locations.site??)>
   <#assign site = locations.site>   
   "site":
   {
      <#if (site.file??)>"file": "${site.file}",</#if>
      "site": "${site.site!""}",
      "siteTitle": "${site.siteTitle!""}",
      "container": "${site.container!""}",
      "path": "${site.path!""}"
   },</#if>
   "repo":
   {
      <#if (locations.repo.file??)>"file": "${locations.repo.file}",</#if>
      "path": "${locations.repo.path!""}"
   }
}
</#escape>
