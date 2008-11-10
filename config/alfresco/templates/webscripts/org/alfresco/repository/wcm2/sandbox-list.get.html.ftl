{
	"results" : [

<#assign first = true>
<#list results as result>
	<#if first == false>,</#if>

		{
			"name" : "${result.name}"
		}

	<#assign first = false>
</#list>

	]
}
