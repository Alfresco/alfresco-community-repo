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

   "docfolder": "${result.docfolder}",
   "recurrence": "${result.recurrence}",
   "isoutlook": "${result.isoutlook?string}",

   "permissions":
   {
       "edit": ${result.canEdit?string},
       "delete": ${result.canDelete?string}
   }
</#if>
}
</#escape>
