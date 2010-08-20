<#-- Workflow Task Instance in details -->

<#import "task.lib.ftl" as taskLib />
{
   "data": 
   <@taskLib.taskJSON task=workflowTask detailed=true/>
}