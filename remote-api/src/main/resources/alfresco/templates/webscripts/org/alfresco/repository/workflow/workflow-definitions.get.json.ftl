<#import "workflow.lib.ftl" as workflowLib />
{
   "data": 
   [
      <#list workflowDefinitions as workflowDefinition>
         <@workflowLib.workflowDefinitionJSON workflowDefinition=workflowDefinition />
         <#if workflowDefinition_has_next>,</#if>
      </#list>
   ]
}