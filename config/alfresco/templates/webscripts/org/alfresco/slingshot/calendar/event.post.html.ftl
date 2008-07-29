<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
  "error" : "${result.error}"
<#else>
  "event": {
    "name": "${result.name}",
    "from": "${result.from?string("M/d/yyyy")}",
    "start": "${result.from?string("HH:mm")}",
    "to": "${result.to?string("M/d/yyyy")}",
    "end": "${result.to?string("HH:mm")}",
    "uri": "${result.uri}"
  }
</#if>
}
</#escape>


