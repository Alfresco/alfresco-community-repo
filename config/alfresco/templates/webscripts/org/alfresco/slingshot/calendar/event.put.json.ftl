{
<#if result.error??>
   "error": "${result.error}"
<#else>
   "data": {
       "summary" : "${result.summary?html}",
       "location" : "${result.location?html}",
       "description" : "${result.description?html}",
       "dtstart" : "${result.dtstart?html}",
       "dtend" : "${result.dtend?html}",
       "uri" : "${result.uri}",       
       "allday" : "${result.allday?string}",
       "tags" : "${result.tags?html}",
       "docfolder": "${result.docfolder?html}"
   }
   
</#if>
}