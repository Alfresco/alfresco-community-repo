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
        "iso8601": "${result.from}",
        "legacyDate": "${result.legacyDateFrom}",
        "legacyTime": "${result.legacyTimeFrom}"
    },
    "endAt": {
        "iso8601": "${result.to}",
        "legacyDate": "${result.legacyDateTo}",
        "legacyTime": "${result.legacyTimeTo}"
    },
    "allday": "${result.allday?string}",

    <#assign tags><#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
    "tags": <#noescape>[${tags}]</#noescape>,
    "docfolder": "${result.docfolder?html}"
  }
</#if>
}
</#escape>


