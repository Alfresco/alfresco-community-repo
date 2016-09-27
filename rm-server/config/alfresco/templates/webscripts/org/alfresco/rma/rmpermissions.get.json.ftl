<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"permissions":
		[
			<#list permissions as perm>
			{
				"id": "${perm.id}",
				"authority":
				{
					"id": "${perm.authority.id}",
					"label": "${perm.authority.label}"
				},
				"inherited": ${perm.inherited?string}
			}<#if perm_has_next>,</#if>
			</#list>
		],
		"inherited": ${inherited?string}
	}
}
</#escape>