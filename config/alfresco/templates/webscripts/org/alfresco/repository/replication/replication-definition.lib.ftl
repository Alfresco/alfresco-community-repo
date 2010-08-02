<#-- Renders a simple replication definition. -->
<#macro simpleReplicationDefinitionJSON replicationDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name": "${replicationDefinition.name}",
         "status" : "${replicationDefinition.status}",
         "startedAt" : <#if replicationDefinition.startedAt??>"${replicationDefinition.startedAt}"<#else>null</#if>,
         "enabled" : ${replicationDefinition.enabled?string},
         "details": "${"/api/replication-definition/" + replicationDefinition.name}",
      }
</#escape>
</#macro>

<#-- Renders the details of a replication definition. -->
<#macro replicationDefinitionJSON replicationDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name": "${replicationDefinition.name}",
         "status" : "${replicationDefinition.status}",
         "enabled" : ${replicationDefinition.enabled?string},
         <#-- TODO The rest of the fields -->
      }
</#escape>
</#macro>
