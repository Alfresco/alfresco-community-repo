<#-- Workflow Instances collection -->

<#import "task.lib.ftl" as taskLib />
{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@taskLib.workflowInstanceJSON workflowInstance=workflowInstance />
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
   <#if paging??>,
   "paging": 
   <@taskLib.pagingJSON paging=paging />
   </#if>
}