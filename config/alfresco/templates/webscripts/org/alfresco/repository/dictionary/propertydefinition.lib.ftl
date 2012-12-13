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
		[<#--
		<#if propertydefs.constraints?exists>
			<#list propertydefs.constraints as constraintdefs>
		{ 
				<#assign keys = constraintdefs.getConstraint()?keys>
				<#list keys as key>
					<#if key == "expression">
			"${key}" : <#if constraintdefs.getConstraint()[key]?exists>"${constraintdefs.getConstraint()[key]}" <#else>"has no value"</#if>
					</#if>
					<#if key_has_next>,</#if>   
				</#list> 
		}<#if constraintdefs_has_next>,</#if>
			</#list>
		</#if>-->
		],
		"url" : "${"/api/property/" + propertydefs.name.toPrefixString()?replace(":","_")}"
	}
	</#escape>
</#macro>