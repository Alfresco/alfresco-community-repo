<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error??>
   "error": "${result.error}"
<#else>
   "data": {
       "summary" : "${result.summary?js_string}",
       "location" : "${result.location?js_string}",
       "description" : "${result.description?js_string}",

       "startAt": {
           "iso8601": "${xmldate(result.dtstart)}",
           "legacyDateTime": "${result.dtstart?string("yyyy-MM-dd")}T${result.dtstart?string("HH:mm")}",
           "legacyDate": "${result.dtstart?string("yyyy-MM-dd")}",
           "legacyTime": "${result.dtstart?string("HH:mm")}",
       },
       "endAt": {
           "iso8601": "${xmldate(result.dtend)}",
           "legacyDateTime": "${result.dtend?string("yyyy-MM-dd")}T${result.dtend?string("HH:mm")}",
           "legacyDate": "${result.dtend?string("yyyy-MM-dd")}",
           "legacyTime": "${result.dtend?string("HH:mm")}",
       },

       <#-- These are the old ones we'll get rid of soon -->
       "dtstart" : "${result.dtstart?string("yyyy-MM-dd")}T${result.dtstart?string("HH:mm")}",
       "dtend" : "${result.dtend?string("yyyy-MM-dd")}T${result.dtend?string("HH:mm")}",

       "uri" : "${result.uri}",       
       "allday" : "${result.allday?string}",
       "tags" : "${result.tags}",
       "docfolder": "${result.docfolder}"
   }
   
</#if>
}
</#escape>
