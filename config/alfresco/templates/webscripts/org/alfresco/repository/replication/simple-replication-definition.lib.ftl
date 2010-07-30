<#-- Renders a workflow definition. -->
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
