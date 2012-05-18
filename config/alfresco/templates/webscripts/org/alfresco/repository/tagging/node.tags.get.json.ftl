[
	<#list tags as tag>
		"${jsonUtils.encodeJSONString(tag)}"<#if tag_has_next>,</#if>
	</#list>
]