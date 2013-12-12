<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"recordMetaDataAspects":
		[
		<#list aspects as aspect>
			{
		    	"id" : "${aspect.id}",
		    	"value" : "${aspect.value}"
			}
			<#if aspect_has_next>,</#if>
		</#list>
		]
	}
}
</#escape>