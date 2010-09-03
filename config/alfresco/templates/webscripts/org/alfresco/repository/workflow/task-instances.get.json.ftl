<#-- List Workflow Task Instances -->
<#import "workflow.lib.ftl" as workflowLib />
{
   "data": 
   [
      <#list taskInstances as task>
      <@workflowLib.taskJSON task=task />
      <#if task_has_next>,</#if>
      </#list>
   ]
   <#if paging??>,
   "paging": 
   <@workflowLib.pagingJSON paging=paging />
   </#if>
}