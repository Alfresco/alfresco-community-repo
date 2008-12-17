<#escape x as jsonUtils.encodeJSONString(x)>
{
   "action" : "${action}",
   <#if inviteId??>
   "inviteId" : "${inviteId}",
   </#if>
   <#if inviteTicket??>
      "inviteTicket" : "${inviteTicket}",
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
</#escape>