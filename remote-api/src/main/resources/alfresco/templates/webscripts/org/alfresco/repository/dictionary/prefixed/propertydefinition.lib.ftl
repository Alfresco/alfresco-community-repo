<#macro propertyDefJSON propertydefs>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		<#if propertydefs.name?exists>
		"name" : "${propertydefs.name.toPrefixString()}",
		</#if>
		<#if propertydefs.getTitle(messages)?has_content>
		"title" : "${propertydefs.getTitle(messages)}",
		</#if>
		<#if propertydefs.getDescription(messages)?has_content>
		"description" : "${propertydefs.getDescription(messages)}",
		</#if>
		<#if propertydefs.defaultValues?exists>
		"defaultValues" : "${propertydefs.defaultValues}",
		<#else>
		"defaultValues" : "",
		</#if>
		<#if propertydefs.dataType?exists>
		"dataType" : "${propertydefs.dataType.name.toPrefixString()}",
		</#if>
		"multiValued" : ${propertydefs.multiValued?string},
		"mandatory" : ${propertydefs.mandatory?string},
		"enforced" : ${propertydefs.mandatoryEnforced?string},
		"protected" : ${propertydefs.protected?string},
		"indexed" : ${propertydefs.indexed?string},
		"indexedAtomically" : ${propertydefs.indexedAtomically?string},
		"constraints" :
		[
			<#list propertydefs.constraints as constraintdefs>
		{ 
			"type" : "${constraintdefs.getConstraint().getType()}",
			"parameters" : 
			[
				<#assign params = constraintdefs.getConstraint().getParameters()>
				<#assign keys = params?keys>
				<#list keys as key>
				{
					"${key}" : <#rt><#if params[key]?is_enumerable>[<#list params[key] as mlist>"${mlist}"<#if mlist_has_next>,</#if></#list>]
								<#t><#else><#if params[key]?is_boolean>${params[key]?string}<#else>"${params[key]?string}"</#if></#if>
				}
					<#if key_has_next>,</#if>
				</#list> 
			]
		}<#if constraintdefs_has_next>,</#if>
			</#list>
		],
		"url" : "${"/api/property/" + propertydefs.name.toPrefixString()?replace(":","/")}"
	}
	</#escape>
</#macro>