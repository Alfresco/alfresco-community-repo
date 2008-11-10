{
	<@serialize object=object includeChildren=includeChildren includeContent=includeContent/>
}

<#macro serialize object includeChildren includeContent>
	"isContainer" : ${object.isContainer?string}
	,
	"isDocument" : ${object.isDocument?string}
	,
	"url" : "${object.url}"
	,
	"downloadUrl" : "${object.downloadUrl}"
	
<#if object.mimetype?exists>	
	,
	"mimetype" : "${object.mimetype}"
</#if>	
	,
	"size" : "${object.size}"
	,
	"displayPath" : "${object.displayPath}"
	,
	"qnamePath" : "${object.qnamePath}"
	,
	"icon16" : "${object.icon16}"
	,
	"icon32" : "${object.icon32}"
	,
	"isLocked" : ${object.isLocked?string}
	,
	"id" : "${object.id}"
	,
	"nodeRef" : "${object.nodeRef}"
	,
	"name" : "${object.name}"
	,
	"type" : "${object.type}"
	,
	"isCategory" : ${object.isCategory?string}
	
<#if object.properties?exists>
	,
	"properties" :
	{
	<#assign first = true>
	<#list object.properties?keys as key>
		<#if object.properties[key]?exists>
			<#assign val = object.properties[key]>
			<#if val?is_string == true>
				<#if first == false>,</#if>
				"${key}" : "${val?js_string}"
				<#assign first = false>
			<#elseif val?is_date == true>
				<#if first == false>,</#if>
				"${key}" : "${val?datetime}"
				<#assign first = false>
			<#elseif val?is_boolean == true>
				<#if first == false>,</#if>
				"${key}" : "${val?string}"
				<#assign first = false>
			</#if>
		</#if>
	</#list>
	}
</#if>

<#if includeChildren && object.children?exists>
	,
	"children" :
	[
		<#assign first = true>
		<#list object.children as child>
			<#if first == false>
		,
			</#if>
		{
			<@serialize object=child includeChildren=false includeContent=includeContent/>
		}
			<#assign first = false>	
		</#list>
	]
<#else>
	,
	"children" : []
</#if>

</#macro> 