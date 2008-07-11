<#if action == "start">
   <p>Invite to join site ${siteShortName} has been sent to ${inviteeUserName}</p>
<#elseif action == "cancel">
   <p>Invite with ID ${inviteId} has been cancelled</p>
<#else>
   <p>Error: unknown invite action ${action}</p>
</#if> 
