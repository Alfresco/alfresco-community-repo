<#if action == "start">
   <p>Invite to join site ${siteShortName} has been sent to ${inviteeUserName}</p>
<#elseif action == "cancel">
   <p>Invite process with workflow ID ${workflowId} has been cancelled</p>
<#else>
   <p>Error: unknown invite action ${action}</p>
</#if> 
