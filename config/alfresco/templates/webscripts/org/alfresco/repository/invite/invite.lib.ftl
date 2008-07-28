<#macro inviteJSON invite>
{
   <#if invite.inviteId??>
      "inviteId" : "${invite.inviteId}",
   <#else>
      "inviteId" : null,
   </#if>
   <#if invite.inviterUserName??>
      "inviterUserName" : "${invite.inviterUserName}",
   <#else>
      "inviterUserName" : null,
   </#if>
   <#if invite.inviteeUserName??>
      "inviteeUserName" : "${invite.inviteeUserName}",
   <#else>
      "inviteeUserName" : null,
   </#if>
   <#if invite.siteShortName??>
      "siteShortName" : "${invite.siteShortName}"
   <#else>
      "siteShortName" : null
   </#if>
}
</#macro>
