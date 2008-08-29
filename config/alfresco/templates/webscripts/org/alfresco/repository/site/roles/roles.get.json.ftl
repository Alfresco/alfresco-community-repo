<#escape x as jsonUtils.encodeJSONString(x)>
{
	"siteRoles":
	[
	   <#list siteRoles as siteRole>"${siteRole}"<#if siteRole_has_next>,</#if></#list>
	],
	"permissionGroups":
	[
	   <#list sitePermissionGroups?keys?sort?reverse as role>"${sitePermissionGroups[role]}"<#if role_has_next>,</#if></#list>
	]
}
</#escape>