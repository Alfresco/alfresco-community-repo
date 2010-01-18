{
	"code" : "${code}"

<#if object?exists>
	,
	"data" : {
		<@serialize object=object includeChildren=includeChildren includeContent=includeContent/>
	}
</#if>
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
	   <@serializeHash hash=object.properties/>
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

<#if isUser && object.associations["cm:avatar"]?exists>
	,
	"associations" :
	{
		"{http://www.alfresco.org/model/content/1.0}avatar" : ["${object.associations["cm:avatar"][0].nodeRef}"]
	}
</#if>

<#if isUser>
    ,
    "capabilities" :
    {
        <@serializeHash hash=capabilities/>
    }
</#if>

</#macro>

<#macro serializeHash hash>

<#local first = true>
<#list hash?keys as key>
    <#if hash[key]?exists>
        <#local val = hash[key]>
        <#if isUser && object.isTemplateContent(val)>
            <#if first == false>,</#if>
            "${key}" : "${jsonUtils.encodeJSONString(val.content)}"
            <#local first = false>
        <#elseif object.isTemplateNodeRef(val)>
            <#if first == false>,</#if>
            "${key}" : "${val.nodeRef}"
            <#local first = false>
        <#elseif val?is_string == true>
            <#if first == false>,</#if>
            "${key}" : "${jsonUtils.encodeJSONString(val)}"
            <#local first = false>
        <#elseif val?is_date == true>
            <#if first == false>,</#if>
            "${key}" : "${val?datetime}"
            <#local first = false>
        <#elseif val?is_boolean == true>
            <#if first == false>,</#if>
            "${key}" : "${val?string}"
            <#local first = false>
        </#if>
    </#if>
</#list>

</#macro>