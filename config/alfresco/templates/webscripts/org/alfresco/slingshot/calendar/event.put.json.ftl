{
<#if result.error??>
   "error": "${result.error}"
<#else>
   "data": {
       "summary" : "${result.summary}",
       "location" : "${result.location}",
       "description" : "${result.description}",
       "dtstart" : "${result.dtstart}",
       "dtend" : "${result.dtend}",
       "uri" : "${result.uri}",       
       "allday" : "${result.allday?string}",
       "tags" : "${result.tags}",
       "docfolder": "${result.docfolder}"
   }
   
</#if>
}