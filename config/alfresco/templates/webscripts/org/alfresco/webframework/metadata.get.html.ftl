{
	"isContainer" : ${object.isContainer?string}
	,
	"isDocument" : ${object.isDocument?string}
	,
	"url" : "${object.url}"
	,
	"downloadUrl" : "${object.downloadUrl}"
	,
	"mimetype" : "${mimetype}"
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
	"id" : ${object.id}
	,
	"nodeRef" : "${object.nodeRef}"
	,
	"name" : "${object.name}"
	,
	"type" : "${object.type}"
	,
	"isCategory" : ${object.isCategory?string}
	
<#if properties?exists>
	,
	"properties" :
	{
	<#assign first = false>
	<#list properties?keys as key>
		<#assign val = properties[key]>
		<#if val?exists>
			<#if first == true>
				,
			</#if>
			"${key}" : "${val}"
			<#assign first = true>
		</#if>

	</#list>
	}
</#if>

}