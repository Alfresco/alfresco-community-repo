<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list attachmentsAssocs as a>
	{
		"nodeRef": "${a.nodeRef}",
		"name": "${a.name}",
		"assocname": "${a.assocname}"
	}<#if (a_has_next)>,</#if>
</#list>
]
</#escape>