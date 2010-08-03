<#-- Workflow Instance in details -->

<#import "task.lib.ftl" as taskLib />
{
   "data": <@taskLib.workflowInstanceJSON workflowInstance=workflowInstance detailed=true/>
}