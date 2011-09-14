<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
  "error" : "${result.error}"
<#else>
  "event":
  {
    "name": "${result.name?html}",
    "uri": "${result.uri}",
    "desc": "${result.desc?html}",
    "where": "${result.where?html}",
    "startAt": {
        "iso8601": "${xmldate(result.from)}",
        "legacyDate": "${result.from?string("yyyy-MM-dd")}",
        "legacyTime": "${result.from?string("HH:mm")}",
    },
    "endAt": {
        "iso8601": "${xmldate(result.to)}",
        "legacyDate": "${result.to?string("yyyy-MM-dd")}",
        "legacyTime": "${result.to?string("HH:mm")}",
    },
    "allday": "${result.allday?string}",

    <#assign tags><#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
    "tags": <#noescape>[${tags}]</#noescape>,
    "docfolder": "${result.docfolder?html}"
  }
</#if>
}
</#escape>


