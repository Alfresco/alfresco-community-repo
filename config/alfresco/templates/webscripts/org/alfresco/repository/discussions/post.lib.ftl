<#--

	This template renders a post.

-->
<#macro postJSON post>
{
	<@postDataJSON refNode=post post=post/>
}
</#macro>

<#-- Adds a modified date property if the creation date and modification
     date differ of more 1 minute.
-->

<#--

<#macro addModifiedDate post>
<#assign min_difference=60000>
<#if post.properties.modified?? && post.properties.modified.time < 100000000 >
	<#- - && ((post.properties.modified.getTime() - post.properties.created.getTime()) < min_difference)> - ->
	"modifiedOn" : "${post.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
</#if>
</#macro>

<@addModifiedDate post=post />

-->

<#macro addContent post>
	<#assign maxTextLength=512>
	<#if (! contentFormat??) || contentFormat == "" || contentFormat == "full">
		"content" : "${post.content?j_string}",
	<#elseif contentFormat == "htmlDigest">
		<#if (post.content?length > maxTextLength)>
			"content" : "${post.content?substring(0, maxTextLength)?j_string}",
		<#else>
			"content" : "${post.content?j_string}",
		</#if>
	<#elseif contentFormat == "textDigest">
		<#assign croppedTextContent=cropContent(post.properties.content, maxTextLength)>
		"content" : "${croppedTextContent?j_string}",
	<#else>
		<#-- no content returned -->
	</#if>
</#macro>


<#macro postDataJSON refNode post>
	<#-- data using refNode which might be the topic or the post node -->
	"url" : "/forum/post/node/${refNode.nodeRef.storeRef.protocol}/${refNode.nodeRef.storeRef.identifier}/${refNode.nodeRef.id}",
	"repliesUrl" : "/forum/post/node/${refNode.nodeRef.storeRef.protocol}/${refNode.nodeRef.storeRef.identifier}/${refNode.nodeRef.id}/replies",
	"nodeRef" : "${refNode.nodeRef?j_string}",
	<#-- normal data, the post node will used to fetch it -->
	"title" : "${(post.properties.title!"")?j_string}",
	"createdOn" : "${post.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn" : "${post.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"author" : "${post.properties.creator?j_string}",
	"isUpdated" : ${post.hasAspect("cm:contentupdated")?string},
	<#if (post.hasAspect("cm:contentupdated"))>
	   "updatedOn" : "${post.properties["cm:contentupdatedate"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	</#if>
	<@addContent post=post />
	"replyCount" : <#if post.sourceAssocs["cm:references"]??>${post.sourceAssocs["cm:references"]?size?c}<#else>0</#if>,
	"permissions" : { "edit": true, "delete" : true, "reply" : true }
</#macro>


<#-- Renders replies.
	The difference is to a normal post is that the children might be
	added inline in the returned JSON.
-->
<#macro repliesJSON data>
{
	<@postDataJSON refNode=data.post post=data.post />
	<#if data.children?exists>
		, "children": <@repliesRootJSON children=data.children />
	</#if>
}
</#macro>

<#macro repliesRootJSON children>
[
	<#list children as child>
		<@repliesJSON data=child/>
		<#if child_has_next>,</#if>
	</#list>
]
</#macro>
