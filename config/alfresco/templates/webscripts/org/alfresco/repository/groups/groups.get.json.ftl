<#-- list / search / groups -->

<#import "authority.lib.ftl" as authorityLib/>
{
	"data": [
		<#list groups as group>
			<@authorityLib.authorityJSON authority=group />
			<#if group_has_next>,</#if>
		</#list>
	]
}