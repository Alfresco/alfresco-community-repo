<#import "rule.lib.ftl" as ruleLib/>
{
	"data" : 
	[
	<#list inheritedRuleRefs as inheritedRuleRef>
		<@ruleLib.ruleRefOwningSummaryJSON ruleRef=inheritedRuleRef />
		<#if inheritedRuleRef_has_next>,</#if> 
	</#list>
	]
}