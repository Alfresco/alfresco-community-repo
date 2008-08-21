{
   "action" : "${action}",
   <#if inviteId??>
   "inviteId" : "${inviteId}",
   </#if>
   <#if inviteeUserName??>
      "inviteeUserName" : "${inviteeUserName}",
   </#if>
   <#if inviteeFirstName??>
      "inviteeFirstName" : "${inviteeFirstName}",
   </#if>
   <#if inviteeLastName??>
      "inviteeLastName" : "${inviteeLastName}",
   </#if>
   <#if inviteeEmail??>
      "inviteeEmail" : "${inviteeEmail}",
   </#if>
   <#if siteShortName??>
      "siteShortName" : "${siteShortName}"
   </#if>
}