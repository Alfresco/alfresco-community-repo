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
   <#if siteShortName??>
      "siteShortName" : "${siteShortName}"
   <#else>
      "siteShortName" : undefined
   </#if>
}