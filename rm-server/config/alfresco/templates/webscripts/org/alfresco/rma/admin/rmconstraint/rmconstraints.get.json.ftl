<#import "rmconstraint.lib.ftl" as rmconstraintLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	[
		<#list constraints as constraint>   
		<@rmconstraintLib.constraintSummaryJSON constraint=constraint />        
		<#if constraint_has_next>,</#if>
		</#list>
	]
}
</#escape>