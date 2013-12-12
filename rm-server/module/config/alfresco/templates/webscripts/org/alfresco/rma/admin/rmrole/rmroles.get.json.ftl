<#import "rmrole.lib.ftl" as rmRoleLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		<#list roles as role>
		"${role.name}":
		<@rmRoleLib.roleJSON role=role /><#if role_has_next>,</#if>
		</#list>
	}
}
</#escape>