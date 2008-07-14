{
   "action" : "${action}",
   <#if inviteId??>
      "inviteId" : "${inviteId}",
   <#else>
      "inviteId" : undefined,
   </#if>
   <#if inviteeUserName??>
      "inviteeUserName" : "${inviteeUserName}",
   <#else>
      "inviteeUserName" : undefined,
   </#if>
   <#if inviteeFirstName??>
      "inviteeFirstName" : "${inviteeFirstName}",
   <#else>
      "inviteeFirstName" : undefined,
   </#if>
   <#if inviteeLastName??>
      "inviteeLastName" : "${inviteeLastName}",
   <#else>
      "inviteeLastName" : undefined,
   </#if>
   <#if inviteeEmail??>
      "inviteeEmail" : "${inviteeEmail}",
   <#else>
      "inviteeEmail" : undefined,
   </#if>
   <#if siteShortName??>
      "siteShortName" : "${siteShortName}"
   <#else>
      "siteShortName" : undefined
   </#if>
}