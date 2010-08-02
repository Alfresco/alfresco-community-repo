<#-- List Workflow Task Instances -->
<#assign detailed=((args.detailed!"false")=="true")>
<#import "task.lib.ftl" as taskLib />
{
   "data": 
   [
      <#list taskInstances as task>
      <@taskLib.taskJSON task=task detailed=detailed/>
      <#if task_has_next>,</#if>
      </#list>
   ]
}