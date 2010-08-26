<#-- Renders a simple replication definition. -->
<#macro simpleReplicationDefinitionJSON replicationDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "name": "${replicationDefinition.name}",
      "status": "${replicationDefinition.status}",
      "startedAt": <#if replicationDefinition.startedAt??>{ "iso8601": "${replicationDefinition.startedAt}" }<#else>null</#if>,
      "enabled": ${replicationDefinition.enabled?string},
      "details": "${"/api/replication-definition/" + replicationDefinition.name}"
   }
</#escape>
</#macro>

<#-- Renders the details of a replication definition. -->
<#macro replicationDefinitionJSON replicationDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "name": "${replicationDefinition.name}",
      "description": "${replicationDefinition.description}",
      "status": "${replicationDefinition.status}",
      "startedAt": <#if replicationDefinition.startedAt??>{ "iso8601": "${replicationDefinition.startedAt}" }<#else>null</#if>,
      "endedAt": <#if replicationDefinition.endedAt??>{ "iso8601": "${replicationDefinition.endedAt}" }<#else>null</#if>,
      "failureMessage": <#if replicationDefinition.failureMessage??>"${replicationDefinition.failureMessage}"<#else>null</#if>,
      "executionDetails": <#if replicationDefinition.runningActionId??>"/api/running-action/${replicationDefinition.runningActionId}"<#else>null</#if>,
      "payload": [
      <#list replicationDefinition.payload as node>
         {
            "nodeRef": "${node.nodeRef}",
            "isFolder": ${node.isContainer?string},
            "name": "${node.name}",
            "path": "${node.displayPath}/${node.name}"
         }<#if node_has_next>,</#if>
      </#list>
      ],
      "transferLocalReport": <#if replicationDefinition.transferLocalReport??>"${replicationDefinition.transferLocalReport.nodeRef}"<#else>null</#if>,
      "transferRemoteReport": <#if replicationDefinition.transferRemoteReport??>"${replicationDefinition.transferRemoteReport.nodeRef}"<#else>null</#if>,
      "enabled": ${replicationDefinition.enabled?string},
      "targetName": <#if replicationDefinition.targetName??>"${replicationDefinition.targetName}"<#else>null</#if>,
      "schedule": <#if replicationDefinition.scheduleEnabled>
          {
            "start": { 
                "iso8601": "${replicationDefinition.scheduleStart}"
            },
            "intervalPeriod": <#if replicationDefinition.scheduleIntervalPeriod??>"${replicationDefinition.scheduleIntervalPeriod}"<#else>null</#if>,
            "intervalCount": <#if replicationDefinition.scheduleIntervalCount??>${replicationDefinition.scheduleIntervalCount}<#else>null</#if>
          }<#else>null</#if>
   }
</#escape>
</#macro>
