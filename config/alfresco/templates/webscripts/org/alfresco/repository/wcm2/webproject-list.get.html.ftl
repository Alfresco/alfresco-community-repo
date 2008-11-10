{
	"results" : [

<#assign first = true>
<#list results as result>
	<#if first == false>,</#if>

		{
			"name" : "${result.name}"
			,
			"nodeRef" : "${result.nodeRef}"
		}

	<#assign first = false>
</#list>

	]
}
