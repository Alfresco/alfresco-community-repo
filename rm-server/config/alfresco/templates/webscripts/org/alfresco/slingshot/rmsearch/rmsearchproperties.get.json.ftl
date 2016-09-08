<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data" : 
	{
		"groups" :
		[
			<#list groups as group>
			{
				"id" : "${group.id}",
				"label" : "${group.label}",
				"properties" : 
				[
				   <#list group.properties as property>
				   {
				      "prefix" : "${property.prefix}",
				      "name" : "${property.shortName}",
				      "label" : "${property.label}",
				      "type" : "${property.type}"
				   }<#if property_has_next>,</#if>
				   </#list>
				]
			}<#if group_has_next>,</#if>
			</#list>
		]
	}	
}
</#escape>