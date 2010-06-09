<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
  "error" : "${result.error}"
<#else>
  "event":
  {
    "name": "${result.name?html}",
    "from": "${result.from?string("yyyy-MM-dd")}",
    "start": "${result.from?string("HH:mm")}",
    "to": "${result.to?string("yyyy-MM-dd")}",
    "end": "${result.to?string("HH:mm")}",
    "uri": "${result.uri}",
    "desc": "${result.desc?html}",
    "where": "${result.where?html}",
    "allday":"${result.allday?html}",
    <#assign tags><#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
    "tags": <#noescape>[${tags}]</#noescape>,
    "docfolder": "${result.docfolder?html}"
  }
</#if>
}
</#escape>


