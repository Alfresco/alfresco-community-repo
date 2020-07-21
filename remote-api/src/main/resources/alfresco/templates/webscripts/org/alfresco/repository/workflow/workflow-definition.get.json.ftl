<#import "workflow.lib.ftl" as workflowLib />
{
   "data": 
       <@workflowLib.workflowDefinitionJSON workflowDefinition=workflowDefinition detailed=true />
}