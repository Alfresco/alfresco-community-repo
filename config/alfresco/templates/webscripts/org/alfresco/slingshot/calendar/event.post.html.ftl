{
<#if result.error?exists>
  "error" : "${result.error}"
<#else>
  "event": {
    "name": "${result.name}",
    "from": "${result.from?string("M/d/yyyy")}"
  }
</#if>
}


