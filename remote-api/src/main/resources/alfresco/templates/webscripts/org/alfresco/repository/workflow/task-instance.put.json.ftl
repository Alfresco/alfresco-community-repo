<#-- Workflow Task Instance in details -->

<#import "workflow.lib.ftl" as workflowLib />
{
   "data": <@workflowLib.taskJSON task=workflowTask detailed=true />
}