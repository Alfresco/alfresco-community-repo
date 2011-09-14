<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
   "error": "${result.error}"
<#else>
   "name": "${result.name}",
   "what": "${result.what}",
   "description": "${result.description}",
   "location": "${result.location}",
   "tags": [<#list result.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
   "startAt": {
       "iso8601": "${xmldate(result.from)}",
       "legacyDate": "${result.from?string("M/d/yyyy")}",
       "legacyTime": "${result.from?string("HH:mm")}",
   },
   "endAt": {
       "iso8601": "${xmldate(result.to)}",
       "legacyDate": "${result.to?string("M/d/yyyy")}",
       "legacyTime": "${result.to?string("HH:mm")}",
   },
   "allday": "${result.allday?string}",

   "docfolder": "${result.docfolder}",
   "recurrence": "${result.recurrence}",
   "isoutlook": "${result.isoutlook?string}"
</#if>
}
</#escape>
