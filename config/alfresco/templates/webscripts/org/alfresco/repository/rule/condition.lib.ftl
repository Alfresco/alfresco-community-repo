<#macro conditionJSON condition>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "id" : "${condition.id}",
         "conditionDefinitionName" : "${condition.actionConditionDefinitionName}",
         "invertCondition" : "${condition.invertCondition?string("true", "false")}",
         "parameterValues" :
         {
         <#list condition.parameterValues.keySet() as parameterKey>
            "${parameterKey}" : "${condition.getParameterValue(parameterKey)}"
            <#if parameterKey_has_next>,</#if>
         </#list>
         },            
         "url" : "${url.serviceContext + "/api/rule/" + rule.nodeRef.storeRef.protocol + "/"
            + rule.nodeRef.storeRef.identifier + "/" + rule.nodeRef.id + "/conditions/"
            + condition.id}"
      }
   </#escape>
</#macro>
