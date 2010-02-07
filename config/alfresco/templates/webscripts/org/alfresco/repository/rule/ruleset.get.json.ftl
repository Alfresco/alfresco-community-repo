<#import "rule.lib.ftl" as ruleLib/>

{
	"data" :
	{
		<#if ruleset.rules?? && ruleset.rules?size &gt; 0>
		"rules" : 
		[
		<#list ruleset.rules as ruleRef>
			{
				"id" : "${ruleRef.rule.nodeRef.id}",
			    "title" : "${ruleRef.rule.title}",
			    <#if ruleRef.rule.description??>
			    "description" : "${ruleRef.rule.description}",
			    </#if>
			    "ruleType" : [<#list ruleRef.rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],    
			    "disabled" : ${ruleRef.rule.ruleDisabled?string},    
			    "url" : "${"/api/node/" + ruleRef.owningNodeRef.storeRef.protocol + "/" + ruleRef.owningNodeRef.storeRef.identifier + "/" + ruleRef.owningNodeRef.id + "/ruleset/rules/" + ruleRef.rule.nodeRef.id}"
			}<#if ruleRef_has_next>,</#if>
		</#list>
		],
		</#if>
		<#if ruleset.inheritedRules?? && ruleset.inheritedRules?size &gt; 0>
		"inheritedRules" : 
		[
		<#list ruleset.inheritedRules as inheritedRuleRef>
			{
				"id" : "${inheritedRuleRef.rule.nodeRef.id}",
			    "title" : "${inheritedRuleRef.rule.title}",
			    <#if inheritedRuleRef.rule.description??>
			    "description" : "${inheritedRuleRef.rule.description}",
			    </#if>
			    "ruleType" : [<#list inheritedRuleRef.rule.ruleTypes as ruleType>"${ruleType}"<#if (ruleType_has_next)>, </#if></#list>],    
			    "disabled" : ${inheritedRuleRef.rule.ruleDisabled?string},    
			    "url" : "${"/api/node/" + inheritedRuleRef.owningNodeRef.storeRef.protocol + "/" + inheritedRuleRef.owningNodeRef.storeRef.identifier + "/" + inheritedRuleRef.owningNodeRef.id + "/ruleset/rules/" + inheritedRuleRef.rule.nodeRef.id}"
			}<#if inheritedRuleRef_has_next>,</#if>
		</#list>
		],
		</#if>
		<#if ruleset.linkedToRuleSet??>
		"linkedToRuleSet" : "${ruleset.linkedToRuleSet}",		
		</#if>
		<#if ruleset.linkedFromRuleSets?? && ruleset.linkedFromRuleSets?size &gt; 0>
		"linkedFromRuleSets" : 
		[
		<#list ruleset.linkedFromRuleSets as linkedFromRuleSet>
			"${linkedFromRuleSet}"<#if linkedFromRuleSet_has_next>,</#if>
		</#list>
		],
		</#if>
		"url" : "${"/api/node/" + ruleset.rulesetNodeRef.storeRef.protocol + "/" + ruleset.rulesetNodeRef.storeRef.identifier + "/" + ruleset.rulesetNodeRef.id + "/ruleset"}"
	}
}