<#import "condition.lib.ftl" as conditionLib/>

<#macro actionJSON action rule>
{
   "id" : "${action.id}",
   "actionDefinitionName" : "${action.actionDefinitionName}",
   <#if action.title?exists>
      "title" : "${action.title}",
   </#if>
   <#if action.description?exists>
      "description" : "${action.description}",
   </#if>
   "executeAsync" : "${action.executeAsychronously?string("true", "false")}",
   <#if action.parameterValues?exists>
      "parameterValues" :
      {
      <#list action.parameterValues?keys as parameterKey>
         "${parameterKey}" : "${action.parameterValues[parameterKey]}"
         <#if parameterKey_has_next>,</#if>
      </#list>
      }, 
   </#if>
   <#if action.actions?exists>
      "actions" :
      {
         <#list action.actions as nestedAction>
            "${nestedAction.id}" : <@actionJSON action=nestedAction rule=rule/>
            <#if nestedAction_has_next>,</#if>
         </#list>
      },            
   </#if>
   <#if action.actionConditions?exists>
    "conditions" :
    {
    <#list action.actionConditions as condition>
       "${condition.id}" : <@conditionLib.conditionJSON condition=condition rule=rule/>
         <#if condition_has_next>,</#if>
      </#list>
      },            
   </#if>
   <#if action.compensatingAction?exists>
      "compensatingAction" : <@actionJSON action=action.compensatingAction/>,
   </#if>
   "url" : "${url.serviceContext + "/api/rules/" + rule.nodeRef.storeRef.protocol + "/"
      + rule.nodeRef.storeRef.identifier + "/" + rule.nodeRef.id + "/actions/"
      + action.id}"
}
</#macro>
