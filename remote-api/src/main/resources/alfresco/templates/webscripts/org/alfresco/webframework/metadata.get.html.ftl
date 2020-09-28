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

<#macro serializeSequence sequence>
<#escape x as jsonUtils.encodeJSONString(x)>
[
<#local first = true>
<#list sequence as e>
   <#if !first>,<#else><#local first = false></#if>
   <#if isUser && object.isTemplateContent(e)>"${e.content}"
   <#elseif object.isTemplateNodeRef(e)>"${e.nodeRef}"
   <#elseif e?is_date>"${xmldate(e)}"
   <#elseif e?is_boolean>${e?string}
   <#elseif e?is_number>${e?c}
   <#else>"${e}"
   </#if>
</#list>
]
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
		<#elseif val?is_date>"${xmldate(val)}"
		<#elseif val?is_boolean>${val?string}
		<#elseif val?is_number>${val?c}
		<#elseif val?is_sequence><@serializeSequence sequence=val/>
		<#else>"${val}"
		</#if>
	</#if>
</#list>
</#escape>
</#macro>