<#macro inviteJSON invite>
{
   <#if invite.inviterUserName??>
      "inviterUserName" : "${invite.inviterUserName}",
   <#else>
      "inviterUserName" : undefined,
   </#if>
   <#if invite.inviteeUserName??>
      "inviteeUserName" : "${invite.inviteeUserName}",
   <#else>
      "inviteeUserName" : undefined,
   </#if>
   <#if invite.siteShortName??>
      "siteShortName" : "${invite.siteShortName}",
   <#else>
      "siteShortName" : undefined,
   </#if>
   <#if invite.inviteId??>
      "inviteId" : "${invite.inviteId}"
   <#else>
      "inviteId" : undefined
   </#if>
}
</#macro>
