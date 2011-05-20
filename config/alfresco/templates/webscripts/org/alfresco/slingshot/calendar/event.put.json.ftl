<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error??>
   "error": "${result.error}"
<#else>
   "data": {
       "summary" : "${result.summary?js_string}",
       "location" : "${result.location?js_string}",
       "description" : "${result.description?js_string}",
       "dtstart" : "${result.dtstart}",
       "dtend" : "${result.dtend}",
       "uri" : "${result.uri}",       
       "allday" : "${result.allday?string}",
       "tags" : "${result.tags}",
       "docfolder": "${result.docfolder}"
   }
   
</#if>
}
</#escape>