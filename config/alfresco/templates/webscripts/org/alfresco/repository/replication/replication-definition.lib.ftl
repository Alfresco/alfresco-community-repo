<#-- Renders a simple replication definition. -->
<#macro simpleReplicationDefinitionJSON replicationDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name": "${replicationDefinition.name}",
         "status" : "${replicationDefinition.status}",
         "enabled" : "${replicationDefinition.enabled}",
         "details": "${replicationDefinition.details_url}",
      }
</#escape>
</#macro>
<#-- Renders the details of a replication definition. -->
<#macro replicationDefinitionJSON replicationDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name": "${replicationDefinition.name}",
         "status" : "${replicationDefinition.status}",
         "enabled" : "${replicationDefinition.enabled}",
         <#-- TODO The rest of the fields -->
      }
</#escape>
</#macro>
