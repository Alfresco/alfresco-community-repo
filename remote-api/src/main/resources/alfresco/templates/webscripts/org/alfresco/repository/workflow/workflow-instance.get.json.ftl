<#-- Workflow Instance in details -->

<#import "workflow.lib.ftl" as workflowLib />
{
   "data": 
   <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance detailed=true />
}