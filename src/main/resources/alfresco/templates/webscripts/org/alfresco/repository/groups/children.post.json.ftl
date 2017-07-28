<#-- post children - add group or user to a group -->

<#import "authority.lib.ftl" as authorityLib/>
{
	<#if group??>
	"data": <@authorityLib.authorityJSON authority=group />
	<#else>
	"data":  { }
	</#if>	
}