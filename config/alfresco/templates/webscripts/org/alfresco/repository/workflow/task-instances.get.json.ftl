<#-- List Workflow Task Instances -->

<#import "task.lib.ftl" as taskLib />
{
   "data": 
   [
      <#list taskInstances as task>
      <@taskLib.taskJSON task=task />
      <#if task_has_next>,</#if>
      </#list>
   ]
}