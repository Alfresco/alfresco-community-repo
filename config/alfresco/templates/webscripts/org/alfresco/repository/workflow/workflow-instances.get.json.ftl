<#-- Workflow Instances collection -->

<#import "workflow.lib.ftl" as workflowLib />
{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance />
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
   <#if paging??>,
   "paging": 
   <@workflowLib.pagingJSON paging=paging />
   </#if>
}