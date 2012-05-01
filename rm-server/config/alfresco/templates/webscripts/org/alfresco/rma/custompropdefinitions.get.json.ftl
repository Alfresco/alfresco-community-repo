<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"customProperties":
		{
			<#list customProps as prop>
			"${prop.name.toPrefixString()}":
			{
				"dataType": "<#if prop.dataType??>${prop.dataType.name.toPrefixString()}</#if>",
				"label": "${prop.title!""}",
				"description": "${prop.description!""}",
				"mandatory": ${prop.mandatory?string},
				"multiValued": ${prop.multiValued?string},
				"defaultValue": "${prop.defaultValue!""}",
				"protected": ${prop.protected?string},
				"propId": "${prop.name.localName}",
				"constraintRefs":
				[
					<#list prop.constraints as con>
					{
						"name": "${con.constraint.shortName!""}",
						"title": "${con.title!""}",
						"type": "${con.constraint.type!""}",
						"parameters":
						{
							<#-- Basic implementation. Only providing 2 hardcoded parameters. -->
							<#assign lov = con.constraint.parameters["allowedValues"]>
							"caseSensitive": ${con.constraint.parameters["caseSensitive"]?string},
							"listOfValues" :
							[
								<#list lov as val>"${val}"<#if val_has_next>,</#if></#list>
							]
						}
					}<#if con_has_next>,</#if>
					</#list>
				]
			}<#if prop_has_next>,</#if>
			</#list>
		}
	}
}
</#escape>