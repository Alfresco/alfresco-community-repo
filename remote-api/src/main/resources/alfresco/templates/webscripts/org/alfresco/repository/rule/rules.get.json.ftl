<#import "rule.lib.ftl" as ruleLib/>
{
	"data" : 
	[
	<#list ruleRefs as ruleRef>
		<@ruleLib.ruleRefOwningSummaryJSON ruleRef=ruleRef />
		<#if ruleRef_has_next>,</#if> 
	</#list>
	]
}