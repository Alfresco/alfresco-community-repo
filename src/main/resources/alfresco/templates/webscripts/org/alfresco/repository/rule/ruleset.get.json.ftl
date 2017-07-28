<#import "rule.lib.ftl" as ruleLib/>

{
	"data" :
	{
		<#if ruleset.rules?? && ruleset.rules?size &gt; 0>
		"rules" : 
		[
		<#list ruleset.rules as ruleRef>
			<@ruleLib.ruleRefOwningSummaryJSON ruleRef=ruleRef />
			<#if ruleRef_has_next>,</#if> 
		</#list>
		],
		</#if>
		<#if ruleset.inheritedRules?? && ruleset.inheritedRules?size &gt; 0>
		"inheritedRules" : 
		[
		<#list ruleset.inheritedRules as inheritedRuleRef>
			<@ruleLib.ruleRefOwningSummaryJSON ruleRef=inheritedRuleRef />
			<#if inheritedRuleRef_has_next>,</#if> 
		</#list>
		],
		</#if>
		<#if ruleset.linkedToRuleSet??>
		"linkedToRuleSet" : "${"/api/node/" + ruleset.linkedToRuleSet.storeRef.protocol + "/" + ruleset.linkedToRuleSet.storeRef.identifier + "/" + ruleset.linkedToRuleSet.id + "/ruleset"}",		
		</#if>
		<#if ruleset.linkedFromRuleSets?? && ruleset.linkedFromRuleSets?size &gt; 0>
		"linkedFromRuleSets" : 
		[
		<#list ruleset.linkedFromRuleSets as linkedFromRuleSet>
			"${"/api/node/" + linkedFromRuleSet.storeRef.protocol + "/" + linkedFromRuleSet.storeRef.identifier + "/" + linkedFromRuleSet.id + "/ruleset"}"<#if linkedFromRuleSet_has_next>,</#if>
		</#list>
		],
		</#if>
		"url" : "${"/api/node/" + ruleset.rulesetNodeRef.storeRef.protocol + "/" + ruleset.rulesetNodeRef.storeRef.identifier + "/" + ruleset.rulesetNodeRef.id + "/ruleset"}"
	}
}