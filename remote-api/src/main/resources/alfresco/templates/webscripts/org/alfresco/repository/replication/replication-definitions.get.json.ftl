<#import "replication-definition.lib.ftl" as replicationDefLib />
{
   "data":
   [
   <#list replicationDefinitions as replicationDefinition>
      <@replicationDefLib.simpleReplicationDefinitionJSON replicationDefinition=replicationDefinition />
      <#if replicationDefinition_has_next>,</#if>
   </#list>
   ]
}