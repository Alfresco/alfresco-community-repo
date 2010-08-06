<#import "running-action.lib.ftl" as actionLib />
{
   "data": 
   [
      <#list runningActions as action>
         <@actionLib.runningActionJSON action=action />
         <#if action_has_next>,</#if>
      </#list>
   ]
}
