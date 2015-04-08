<#assign page = result.page>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${page.systemName}",
   "title": "${page.title}",
   "pagetext": "${page.contents}",
   <#assign tags><#list page.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
   "tags": <#noescape>[${tags}]</#noescape>
}
</#escape>
