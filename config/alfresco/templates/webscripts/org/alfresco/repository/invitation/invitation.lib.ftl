<#-- renders an invitation object which can be either a MODERATED or NOMINATED invitation-->
<#macro invitationJSON invitation avatars={"" : ""} >
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "inviteId": "${invitation.inviteId}",
   "inviteeUserName": "${invitation.inviteeUserName}",
   "invitee":
   {
      <#if invitation.inviteeFirstName??>"firstName": "${invitation.inviteeFirstName}",</#if>
      <#if invitation.inviteeLastName??>"lastName": "${invitation.inviteeLastName}",</#if>
      <#if invitation.inviteeEmail??>"email": "${invitation.inviteeEmail}",</#if>
      <#assign userName = invitation.inviteeUserName>
      <#if avatars[userName]??>"avatar" : "${avatars[userName]}",</#if>
      "userName": "${invitation.inviteeUserName}"
   },
   <#-- Moderated invitation properties -->
   <#if invitation.inviteeComments??>"inviteeComments": "${invitation.inviteeComments}",</#if>   
   <#if invitation.roleName??>"roleName": "${invitation.roleName}",</#if>      
   <#-- Nominated invitation properties -->
   <#if invitation.acceptURL??>"acceptURL": "${invitation.acceptURL}",</#if>
   <#if invitation.rejectURL??>"rejectURL": "${invitation.rejectURL}",</#if>
   <#if invitation.sentInviteDateAsISO8601??>
   "sentInviteDate" : 
   {
       "iso8601" : "${invitation.sentInviteDateAsISO8601}" 
    }, 
    </#if>
   <#if invitation.inviterUserName??>"inviterUserName": "${invitation.inviterUserName}",</#if>
   <#-- put a mandatory property at the end to deal cleanly with training commas -->
   "resourceType": "${invitation.resourceType}",
   "resourceName": "${invitation.resourceName}",
   "invitationType": "${invitation.invitationType}"
}
</#escape>
</#macro>
