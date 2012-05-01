<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	[
		<#list items as item>
		{
			"name" : "${item.name}",
			"isAspect" : ${item.isAspect?string},
			"title" : "${item.title}"
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>
