<#macro renderParent node indent="   ">
	<#escape x as jsonUtils.encodeJSONString(x)>
	${indent}"parent":
	${indent}{
	<#if (node != rootNode) && node.parent??>
		<@renderParent node.parent indent+"   " />
	</#if>
		${indent}"type": "${node.typeShort}",
		${indent}"isContainer": ${node.isContainer?string},
		${indent}"name": "${node.properties.name!""}",
		${indent}"title": "${node.properties.title!""}",
		${indent}"description": "${node.properties.description!""}",
		<#if node.properties.modified??>${indent}"modified": "${xmldate(node.properties.modified)}",</#if>
		<#if node.properties.modifier??>${indent}"modifier": "${node.properties.modifier}",</#if>
		${indent}"displayPath": "${node.displayPath!""}",
		${indent}"qnamePath": "${node.qnamePath!""}",
		<#if node.aspects??>
        ${indent}"aspects": 
        ${indent}[
           <#list node.aspects as aspect>
                 "${shortQName(aspect)}"
              <#if aspect_has_next>,</#if>
           </#list>
        
           ${indent}],
       </#if>
		${indent}"nodeRef": "${node.nodeRef}"
	${indent}},
	</#escape>
</#macro>

<#macro pickerResultsJSON results>
	<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
<#if parent??>
	<@renderParent parent />
</#if>
		"items":
		[
		<#list results as row>
			{
				"type": "${row.item.typeShort}",
				"parentType": "${row.item.parentTypeShort!""}",
				"isContainer": ${row.item.isContainer?string},
				<#if row.container??>"container": "${row.container!""}",</#if>
				"name": "${row.item.properties.name!""}",
				<#if row.item.aspects??>
                 "aspects": [
                   <#list row.item.aspects as aspect>
                     "${shortQName(aspect)}"
                      <#if aspect_has_next>,</#if>
                   </#list>
                   ],
                 </#if>
				"title":<#if row.item.properties["lnk:title"]??>"${row.item.properties["lnk:title"]}",
						<#elseif row.item.properties["ia:whatEvent"]??>"${row.item.properties["ia:whatEvent"]}",
						<#else>"${row.item.properties.title!""}",</#if>
				"description": "${row.item.properties.description!""}",
				<#if row.item.properties.modified??>"modified": "${xmldate(row.item.properties.modified)}",</#if>
				<#if row.item.properties.modifier??>"modifier": "${row.item.properties.modifier}",</#if>
				<#if row.item.siteShortName??>"site": "${row.item.siteShortName}",</#if>
				<#if row.item.properties["ia:fromDate"]??>"fromDate": "${xmldate(row.item.properties["ia:fromDate"])}",</#if>
				"displayPath": "${row.item.displayPath!""}",
				"qnamePath": "${row.item.qnamePath!""}",
				<#if row.item.typeShort != "cm:person" && row.item.typeShort != "cm:authorityContainer">
					"userAccess":
					{
						"create": ${row.item.hasPermission("CreateChildren")?string},
						"edit": ${row.item.hasPermission("Write")?string},
						"delete": ${row.item.hasPermission("Delete")?string}
					},
				</#if>
				"nodeRef": "${row.item.nodeRef}"<#if row.selectable?exists>,
				"selectable" : ${row.selectable?string}</#if>
			}<#if row_has_next>,</#if>
		</#list>
		]
	}
}
	</#escape>
</#macro>