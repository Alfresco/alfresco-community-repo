<#-- List Workflow Task Instances -->
<#import "workflow.lib.ftl" as workflowLib />
<#import "../generic-paged-results.lib.ftl" as genericPaging />
{
   "data": 
   [
      <#list taskInstances as task>
      <@workflowLib.taskJSON task=task />
      <#if task_has_next>,</#if>
      </#list>
   ]

   <@genericPaging.pagingJSON pagingVar="paging" />
}
