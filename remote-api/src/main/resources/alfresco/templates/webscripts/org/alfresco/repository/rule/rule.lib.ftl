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
				"url" : "${"/api/node/" + ruleRef.owningFileInfo.nodeRef.storeRef.protocol + "/" + ruleRef.owningFileInfo.nodeRef.storeRef.identifier + "/" + ruleRef.owningFileInfo.nodeRef.id + "/ruleset/rules/" + ruleRef.rule.nodeRef.id + "/action/conditions/" + actionCondition.id}"
			}<#if (actionCondition_has_next)>,</#if>
		</#list>
		],
		</#if>
		<#if action.compensatingAction??>
		"compensatingAction" : <@actionJSON action=action.compensatingAction />,
		</#if>
		"url" : "${"/api/node/" + ruleRef.owningFileInfo.nodeRef.storeRef.protocol + "/" + ruleRef.owningFileInfo.nodeRef.storeRef.identifier + "/" + ruleRef.owningFileInfo.nodeRef.id + "/ruleset/rules/" + ruleRef.rule.nodeRef.id + "/action/actions/" + action.id}"
	}
</#escape>
</#macro>

<#-- renders a complete rule object -->
<#macro ruleRefJSON ruleRef>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id" : "${ruleRef.rule.nodeRef.id}",
	    "title" : "${ruleRef.rule.title}",
	    <#if ruleRef.rule.description??>
	    "description" : "${ruleRef.rule.description}",
	    </#if>
	    "ruleType" : [<#list ruleRef.rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],
	    "applyToChildren" : ${ruleRef.rule.appliedToChildren?string},
	    "executeAsynchronously" : ${ruleRef.rule.executeAsynchronously?string},
	    "disabled" : ${ruleRef.rule.ruleDisabled?string},
	    "action" : <@actionJSON action=ruleRef.rule.action />,   
	    "owningNode" :
	    {
	    	"nodeRef" : "${ruleRef.owningFileInfo.nodeRef}",
	    	"name" : "${ruleRef.owningFileInfo.name}"
	    },
	    "url" : "${"/api/node/" + ruleRef.owningFileInfo.nodeRef.storeRef.protocol + "/" + ruleRef.owningFileInfo.nodeRef.storeRef.identifier + "/" + ruleRef.owningFileInfo.nodeRef.id + "/ruleset/rules/" + ruleRef.rule.nodeRef.id}"
	}
</#escape>
</#macro>

<#-- renders a summary rule object with owning nodeRef -->
<#macro ruleRefOwningSummaryJSON ruleRef>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id" : "${ruleRef.rule.nodeRef.id}",
	    "title" : "${ruleRef.rule.title}",
	    <#if ruleRef.rule.description??>
	    "description" : "${ruleRef.rule.description}",
	    </#if>
	    "ruleType" : [<#list ruleRef.rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],    
	    "disabled" : ${ruleRef.rule.ruleDisabled?string},
	    "owningNode" :
	    {
	    	"nodeRef" : "${ruleRef.owningFileInfo.nodeRef}",
	    	"name" : "${ruleRef.owningFileInfo.name}"
	    },    
	    "url" : "${"/api/node/" + ruleRef.owningFileInfo.nodeRef.storeRef.protocol + "/" + ruleRef.owningFileInfo.nodeRef.storeRef.identifier + "/" + ruleRef.owningFileInfo.nodeRef.id + "/ruleset/rules/" + ruleRef.rule.nodeRef.id}"
	}
</#escape>
</#macro>

<#-- renders a summary rule object without owning nodeRef -->
<#macro ruleRefSummaryJSON ruleRef>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id" : "${ruleRef.rule.nodeRef.id}",
	    "title" : "${ruleRef.rule.title}",
	    <#if ruleRef.rule.description??>
	    "description" : "${ruleRef.rule.description}",
	    </#if>
	    "ruleType" : [<#list ruleRef.rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],    
	    "disabled" : ${ruleRef.rule.ruleDisabled?string},
	    "url" : "${"/api/node/" + ruleRef.owningFileInfo.nodeRef.storeRef.protocol + "/" + ruleRef.owningFileInfo.nodeRef.storeRef.identifier + "/" + ruleRef.owningFileInfo.nodeRef.id + "/ruleset/rules/" + ruleRef.rule.nodeRef.id}"
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
		<#elseif val?is_number == true>
		${val?c}
		<#elseif val?is_date == true>
		"${val?string("EEE MMM dd HH:mm:ss zzz yyyy")}"		
		<#elseif val?is_sequence>
		[
			<#list val as element>
			"${jsonUtils.encodeJSONString(element?string)}"<#if (element_has_next)>,</#if>
			</#list>
		]		
		<#else>
		"${jsonUtils.encodeJSONString(shortQName(val?string))}"
		</#if>
		<#if (parameterValue_has_next)>,</#if>				
	</#list>	
</#macro>

<#-- renders a action constraint object -->
<#macro actionConstraintJSON actionConstraint>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"name" : "${actionConstraint.name}",		
		"values" : 
		[
		<#if actionConstraint.allowableValues??>
		   <#assign allowableValues = actionConstraint.allowableValues>
		   
		   <#if allowableValues?size &gt; 0>
			<#list allowableValues?keys as allowableValue>	
			<#assign val = allowableValues[allowableValue]>				
			{
				"value" : "${allowableValue}",
				"displayLabel" : "${val}"
			}<#if allowableValue_has_next>,</#if>				
			</#list>
			</#if>
		</#if>
		]
	}
</#escape>
</#macro>