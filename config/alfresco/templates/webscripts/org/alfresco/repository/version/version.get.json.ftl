[
<#list versions as v>
	{
		nodeRef: "${v.nodeRef}",
		name: "${jsonUtils.encodeJSONString(v.name)}",
		label: "${v.label}",
		description: "${jsonUtils.encodeJSONString(v.description)}",
		createdDate: "${v.createdDate?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
		creator:
		{
			userName: "${v.creator.userName}",
			firstName: "${jsonUtils.encodeJSONString(v.creator.firstName!"")}",
			lastName: "${jsonUtils.encodeJSONString(v.creator.lastName!"")}"
		}
	}<#if (v_has_next)>,</#if>
</#list>
]