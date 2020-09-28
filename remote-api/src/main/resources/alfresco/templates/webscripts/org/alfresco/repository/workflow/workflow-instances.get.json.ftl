<#-- Workflow Instances collection -->

<#import "workflow.lib.ftl" as workflowLib />
<#import "../generic-paged-results.lib.ftl" as genericPaging />
{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance />
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]

   <@genericPaging.pagingJSON pagingVar="paging" />
}
