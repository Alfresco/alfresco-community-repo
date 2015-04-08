<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if (locations.site??)>
   <#assign site = locations.site>   
   "site":
   {
      "site": "${site.site!""}",
      "siteTitle": "${site.siteTitle!""}",
      "container": "${site.container!""}",
      "path": "${site.path!""}",
      "file": "${site.file!""}"
   },</#if>
   "repo":
   {
      "path": "${locations.repo.path!""}",
      "file": "${locations.repo.file!""}"
   }
}
</#escape>