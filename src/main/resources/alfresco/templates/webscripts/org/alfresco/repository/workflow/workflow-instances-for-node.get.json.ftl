<#-- Workflow Instances collection for NodeRef-->

<#import "workflow.lib.ftl" as workflowLib />
{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance />
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
}