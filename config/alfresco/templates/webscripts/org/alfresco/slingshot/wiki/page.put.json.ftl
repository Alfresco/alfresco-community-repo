<#escape x as jsonUtils.encodeJSONString(x)>
{
   "pagetext": "${result.pagetext}",
   <#assign tags><#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
   "tags": <#noescape>[${tags}]</#noescape>
}
</#escape>