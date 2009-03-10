<#-- renders an invitation object which can be either a MODERATED or NOMINATED invitation-->
<#macro invitationJSON invitation>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "inviteId": "${invitation.inviteId}",
   "inviteeUserName": "${invitation.inviteeUserName}",
   <#if invitation.roleName??>"roleName": "${invitation.roleName}",</#if>      
   <#-- Moderated invitation properties -->
   <#if invitation.inviteeComments??>"inviteeComments": "${invitation.inviteeComments}",</#if>   
   <#-- Nominated invitation properties -->
   <#if invitation.acceptURL??>"acceptURL": "${invitation.acceptURL}",</#if>
   <#if invitation.acceptURL??>"rejectURL": "${invitation.rejectURL}",</#if>
   <#if invitation.sentInviteDateAsISO8601??>"sentInviteDate" : { "iso8601" : "${invitation.sentInviteDateAsISO8601}" }, </#if>
   <#if invitation.inviteeFirstName??>"inviteeFirstName": "${invitation.inviteeFirstName}",</#if>
   <#if invitation.inviteeLastName??>"inviteeLastName": "${invitation.inviteeLastName}",</#if>
   <#if invitation.inviteeEmail??>"inviteeEmail": "${invitation.inviteeEmail}",</#if>
   <#-- put a mandatory property at the end to deal cleanly with training commas -->
   "resourceType": "${invitation.resourceType}",
   "resourceName": "${invitation.resourceName}",
   "invitationType": "${invitation.invitationType}"
}
</#escape>
</#macro>
