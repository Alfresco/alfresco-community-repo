<#macro conditionJSON condition rule>
{
   "id" : "${condition.id}",
   "conditionDefinitionName" : "${condition.actionConditionDefinitionName}",
   "invertCondition" : "${condition.invertCondition?string("true", "false")}",
   "parameterValues" :
   {
   <#list condition.parameterValues?keys as parameterKey>
      "${parameterKey}" : "${condition.parameterValues[parameterKey]}"
      <#if parameterKey_has_next>,</#if>
   </#list>
   },            
   "url" : "${url.serviceContext + "/api/rules/" + rule.nodeRef.storeRef.protocol + "/"
      + rule.nodeRef.storeRef.identifier + "/" + rule.nodeRef.id + "/conditions/"
      + condition.id}"
  }
</#macro>
