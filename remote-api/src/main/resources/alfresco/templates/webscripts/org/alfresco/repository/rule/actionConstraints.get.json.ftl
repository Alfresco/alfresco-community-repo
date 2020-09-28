<#import "rule.lib.ftl" as ruleLib/>

{
	"data" : 
	[
	<#list actionConstraints as actionConstraint>		
		<@ruleLib.actionConstraintJSON actionConstraint=actionConstraint />
		<#if actionConstraint_has_next>,</#if>
	</#list>
	]
}