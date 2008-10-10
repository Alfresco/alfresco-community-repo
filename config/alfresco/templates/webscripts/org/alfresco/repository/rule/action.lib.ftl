<#import "condition.lib.ftl" as conditionLib/>

<#macro actionJSON action>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "id" : "${action.id}",
         "actionDefinitionName" : "${action.actionDefinitionName}",
         "title" : "${action.title}",
         "description" : "${action.description}",
         "executeAsync" : "${action.executeAsychronously?string("true", "false")}",
         "parameterValues" :
         {
         <#list action.parameterValues.keySet() as parameterKey>
            "${parameterKey}" : "${action.getParameterValue(parameterKey)}"
            <#if parameterKey_has_next>,</#if>
         </#list>
         },            
         "actions" :
         {
         <#list action.actions as nestedAction>
            "${nestedAction.id}" : <@actionJSON action=nestedAction/>
            <#if nestedAction_has_next>,</#if>
         </#list>
         },            
         "conditions" :
         {
         <#list action.actionConditions as condition>
            "${condition.id}" : <@conditionLib.conditionJSON condition=condition/>
            <#if condition_has_next>,</#if>
         </#list>
         },            
         <#if action.compensatingAction?exists>
            "compensatingAction" : <@actionJSON action=action.compensatingAction/>,
         </#if>,
         "url" : "${url.serviceContext + "/api/rule/" + rule.nodeRef.storeRef.protocol + "/"
            + rule.nodeRef.storeRef.identifier + "/" + rule.nodeRef.id + "/actions/"
            + action.id}"
      }
   </#escape>
</#macro>
