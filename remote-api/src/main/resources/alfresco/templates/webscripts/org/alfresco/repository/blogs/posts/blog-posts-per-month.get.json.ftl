<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items" : [	
		<#list data as item>
			{
				"firstPostInMonth" : "${item.firstPostInMonth?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
				"beginOfMonth" : "${item.beginOfMonth?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
				"endOfMonth" : "${item.endOfMonth?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
				"year" : ${item.year?c},
				"month" : ${item.month?c},  <#-- Note: January -->
				"postCount" : ${item.count?c}
			}
			<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>
