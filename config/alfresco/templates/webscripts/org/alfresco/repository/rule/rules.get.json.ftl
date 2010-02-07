<#import "rule.lib.ftl" as ruleLib/>
{
	"data" : 
	[
	<#list rules as rule>
		<@ruleLib.rulesummaryJSON rule=rule />
		<#if rule_has_next>,</#if> 
	</#list>
	]
}