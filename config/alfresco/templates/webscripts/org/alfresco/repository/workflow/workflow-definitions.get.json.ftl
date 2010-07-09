<#import "workflow-definition.lib.ftl" as workflowDefLib />
{
   "data": 
   [
      <#list workflowDefinitions as workflowDefinition>
         <@workflowDefLib.workflowDefinitionJSON workflowDefinition=workflowDefinition />
         <#if workflowDefinition_has_next>,</#if>
      </#list>
  	]
}