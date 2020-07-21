<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error??>
   "error": "${result.error}"
<#else>
   "data":
   {
       "summary" : "${result.summary?js_string}",
       "location" : "${result.location?js_string}",
       "description" : "${result.description?js_string}",
       "startAt":
      {
           "iso8601": "${result.dtstart}",
           "legacyDateTime": "${result.legacyDateFrom}T${result.legacyTimeFrom}",
           "legacyDate": "${result.legacyDateFrom}",
           "legacyTime": "${result.legacyTimeFrom}"
      },
       "endAt":
      {
           "iso8601": "${result.dtend}",
           "legacyDateTime": "${result.legacyDateTo}T${result.legacyTimeTo}",
           "legacyDate": "${result.legacyDateTo}",
           "legacyTime": "${result.legacyTimeTo}"
      },
      "uri" : "${result.uri}",
      "allday" : "${result.allday?string}",
      "tags" : [<#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
      "docfolder": "${result.docfolder}"
   }
   
</#if>
}
</#escape>
