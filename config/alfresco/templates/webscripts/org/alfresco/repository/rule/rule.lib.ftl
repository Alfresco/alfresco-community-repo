<#-- renders a rule action object -->

<#macro actionJSON action>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id" : "${action.id}",
		"actionDefinitionName" : "${action.actionDefinitionName}",
		<#if action.description??>"description" : "${action.description}",</#if>
		<#if action.title??>"title" : "${action.title}",</#if>
		<#if action.parameterValues?? && action.parameterValues?size &gt; 0>
		"parameterValues" :		
		{
		<@parameterValuesJSON parameterValues=action.parameterValues />
		}, 
		</#if>
		"executeAsync" : ${action.executeAsychronously?string},
		<#if action.runAsUser??>
		"runAsUser" : "${action.runAsUser}",
		</#if>
		<#if action.actions?? && action.actions?size &gt; 0>
		"actions" :
		[
		<#list action.actions as innerAction>
			<@actionJSON action=innerAction />
		<#if innerAction_has_next>,</#if>
		</#list>
		],
		</#if>
		<#if action.actionConditions?? && action.actionConditions?size &gt; 0>
		"conditions" : 
		[
		<#list action.actionConditions as actionCondition>
			{
				"id" : "${actionCondition.id}",
				"conditionDefinitionName" : "${actionCondition.actionConditionDefinitionName}",
				"invertCondition" : ${actionCondition.invertCondition?string},
				<#if actionCondition.parameterValues?? && actionCondition.parameterValues?size &gt; 0>
				"parameterValues" : 
				{			
				<@parameterValuesJSON parameterValues=actionCondition.parameterValues />					
				},
				</#if>
				"url" : "${"/api/node/" + storeType + "/" + storeId + "/" + id + "/ruleset/rules/" + rule.nodeRef.id + "/action/conditions/" + actionCondition.id}"
			}<#if (actionCondition_has_next)>,</#if>
		</#list>
		],
		</#if>
		<#if action.compensatingAction??>
		"compensatingAction" : <@actionJSON action=action.compensatingAction />,
		</#if>
		"url" : "${"/api/node/" + storeType + "/" + storeId + "/" + id + "/ruleset/rules/" + rule.nodeRef.id + "/action/actions/" + action.id}"
	}
</#escape>
</#macro>

<#-- renders a complete rule object -->
<#macro ruleJSON rule>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id" : "${rule.nodeRef.id}",
	    "title" : "${rule.title}",
	    <#if rule.description??>
	    "description" : "${rule.description}",
	    </#if>
	    "ruleType" : [<#list rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],
	    "applyToChildren" : ${rule.appliedToChildren?string},
	    "executeAsynchronously" : ${rule.executeAsynchronously?string},
	    "disabled" : ${rule.ruleDisabled?string},
	    "action" : <@actionJSON action=rule.action />,    
	    "url" : "${"/api/node/" + storeType + "/" + storeId + "/" + id + "/ruleset/rules/" + rule.nodeRef.id}"
	}
</#escape>
</#macro>

<#-- renders a summary rule object -->
<#macro rulesummaryJSON rule>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id" : "${rule.nodeRef.id}",
	    "title" : "${rule.title}",
	    <#if rule.description??>
	    "description" : "${rule.description}",
	    </#if>
	    "ruleType" : [<#list rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],    
	    "disabled" : ${rule.ruleDisabled?string},    
	    "url" : "${"/api/node/" + storeType + "/" + storeId + "/" + id + "/ruleset/rules/" + rule.nodeRef.id}"
	}
</#escape>
</#macro>

<#-- renders parameters values map -->
<#macro parameterValuesJSON parameterValues>	
	<#list parameterValues?keys as parameterValue>	
		<#assign val = parameterValues[parameterValue]>				
		"${parameterValue}" : 
		<#if val?is_boolean == true>
		${val?string}
		<#elseif val?is_date == true>
		"${val?string("EEE MMM dd HH:mm:ss zzz yyyy")}"		
		<#else>
		"${jsonUtils.encodeJSONString(val?string)}"
		</#if>
		<#if (parameterValue_has_next)>,</#if>				
	</#list>	
</#macro>