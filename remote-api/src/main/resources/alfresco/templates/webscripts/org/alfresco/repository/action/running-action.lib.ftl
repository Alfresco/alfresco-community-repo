<#-- Renders the details of a running action. -->
<#macro runningActionJSON action>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "actionId": "${action.id}",
   "actionType": "${action.type}",
   "actionInstance": "${action.instance?string}",
   "actionNodeRef": <#if action.nodeRef??>"${action.nodeRef.nodeRef}"<#else>null</#if>,
   "startedAt": <#if action.startedAt??>"${action.startedAt}"<#else>null</#if>,
   "cancelRequested": "${action.cancelRequested?string}",
   "details": "${"/api/running-action/" + action.key}"
}
</#escape>
</#macro>
