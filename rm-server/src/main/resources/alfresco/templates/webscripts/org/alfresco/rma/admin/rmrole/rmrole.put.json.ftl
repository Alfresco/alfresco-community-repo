<#import "rmrole.lib.ftl" as rmRoleLib/>
   
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	<@rmRoleLib.roleJSON role=role />
}
</#escape>