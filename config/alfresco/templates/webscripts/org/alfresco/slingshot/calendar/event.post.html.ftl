<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
  "error" : "${result.error}"
<#else>
  "event":
  {
    "name": "${result.name}",
    "from": "${result.from?string("yyyy-M-dd")}",
    "start": "${result.from?string("HH:mm")}",
    "to": "${result.to?string("yyyy-M-dd")}",
    "end": "${result.to?string("HH:mm")}",
    "uri": "${result.uri}",
    "desc": "${result.desc}",
    "where": "${result.where}",
    <#assign tags><#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
    "tags": <#noescape>[${tags}]</#noescape>
  }
</#if>
}
</#escape>


