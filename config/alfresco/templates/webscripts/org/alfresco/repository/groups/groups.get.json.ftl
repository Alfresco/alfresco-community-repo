<#-- list / search / groups -->

<#import "authority.lib.ftl" as authorityLib/>
<#import "../generic-paged-results.lib.ftl" as genericPaging />
{
	"data": [
		<#list groups as group>
			<@authorityLib.authorityJSON authority=group />
			<#if group_has_next>,</#if>
		</#list>
	]

   <@genericPaging.pagingJSON />
}
