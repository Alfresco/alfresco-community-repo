<#-- get children -->

<#import "authority.lib.ftl" as authorityLib/>
<#import "../generic-paged-results.lib.ftl" as genericPaging />
{
	"data": [
		<#list children as c>
			<@authorityLib.authorityJSON authority=c />
				<#if c_has_next>,</#if>
		</#list>
	]

   <@genericPaging.pagingJSON />
}
