{
	"code": "${code}"

<#if object?exists>
	,
	"data": {
		<@serialize object=object includeChildren=includeChildren includeContent=includeContent/>
	}
</#if>
}

<#macro serialize object includeChildren includeContent>
<#escape x as jsonUtils.encodeJSONString(x)>
	"isContainer": ${object.isContainer?string}
	,
	"isDocument": ${object.isDocument?string}
	,
	"url": "${object.url}"
	,
	"downloadUrl": "${object.downloadUrl}"
<#if object.mimetype?exists>
	,
	"mimetype": "${object.mimetype}"
</#if>
	,
	"size": "${object.size}"
	,
	"displayPath": "${object.displayPath}"
	,
	"qnamePath": "${object.qnamePath}"
	,
	"icon16": "${object.icon16}"
	,
	"icon32": "${object.icon32}"
	,
	"isLocked": ${object.isLocked?string}
	,
	"id": "${object.id}"
	,
	"nodeRef": "${object.nodeRef}"
	,
	"name": "${object.name}"
	,
	"type": "${object.type}"
	,
	"isCategory": ${object.isCategory?string}
<#if object.properties?exists>
	,
	"properties":
	{
	   <@serializeHash hash=object.properties/>
	}
</#if>

<#if includeChildren && object.children?exists>
	,
	"children":
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
	"children": []
</#if>

<#if isUser && object.associations["cm:avatar"]?exists>
	,
	"associations":
	{
		"{http://www.alfresco.org/model/content/1.0}avatar": ["${object.associations["cm:avatar"][0].nodeRef}"]
	}
</#if>

<#if isUser>
	,
	"capabilities":
	{
		<@serializeHash hash=capabilities/>
	}
	,
	"immutableProperties":
	{
		<@serializeHash hash=immutableProperties/>
	}
</#if>
</#escape>
</#macro>

<#macro serializeHash hash>
<#escape x as jsonUtils.encodeJSONString(x)>
<#local first = true>
<#list hash?keys as key>
	<#if hash[key]??>
		<#local val = hash[key]>
		<#if !first>,<#else><#local first = false></#if>"${key}":
		<#if isUser && object.isTemplateContent(val)>"${val.content}"
		<#elseif object.isTemplateNodeRef(val)>"${val.nodeRef}"
		<#elseif val?is_date>"${val?datetime?string}"
		<#elseif val?is_boolean>${val?string}
		<#else>"${val}"
		</#if>
	</#if>
</#list>
</#escape>
</#macro>