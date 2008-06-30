{
   "action" : "${action}",
   <#if workflowId??>
      "workflowId" : "${workflowId}",
   <#else>
      "workflowId" : undefined,
   </#if>
   <#if inviteeUserName??>
      "inviteeUserName" : "${inviteeUserName}",
   <#else>
      "inviteeUserName" : undefined,
   </#if>
   <#if siteShortName??>
      "siteShortName" : "${siteShortName}"
   <#else>
      "siteShortName" : undefined
   </#if>
}