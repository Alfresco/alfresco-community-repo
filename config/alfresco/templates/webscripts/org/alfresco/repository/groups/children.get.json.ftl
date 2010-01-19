<#-- get children -->

<#import "authority.lib.ftl" as authorityLib/>
{
	"data": [
		<#list children as c>
			<@authorityLib.authorityJSON authority=c />
				<#if c_has_next>,</#if>
		</#list>
	]
}