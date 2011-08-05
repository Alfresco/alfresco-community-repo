<#assign page = result.page>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${page.name}",
   "title": "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
   "pagetext": "${page.content}",
   <#assign tags><#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
   "tags": <#noescape>[${tags}]</#noescape>
}
</#escape>
