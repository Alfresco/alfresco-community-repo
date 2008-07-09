
<#macro renderTags tags>
[<#list tags as x>"${x?j_string}"<#if x_has_next>, </#if></#list>]
</#macro>

<#macro addContent item>
	<#assign maxTextLength=512>
	<#if (! contentFormat??) || contentFormat == "" || contentFormat == "full">
		"content" : "${item.node.content?j_string}",
	<#elseif contentFormat == "htmlDigest">
		<#if (item.node.content?length > maxTextLength)>
			"content" : "${item.node.content?substring(0, maxTextLength)?j_string}",
		<#else>
			"content" : "${item.node.content?j_string}",
		</#if>
	<#elseif contentFormat == "textDigest">
		<#assign croppedTextContent=cropContent(item.node.properties.content, maxTextLength)>
		"content" : "${croppedTextContent?j_string}",
	<#else>
		<#-- no content returned -->
	</#if>
</#macro>

<#--

	This template renders a blog post.

-->
<#macro blogpostJSON item>
{
	"url" : "blog/post/node/${item.node.nodeRef?replace('://','/')}",
	"commentsUrl" : "/node/${item.node.nodeRef?replace('://','/')}/comments",
	"nodeRef" : "${item.node.nodeRef}",
	"name" : "${(item.node.properties.name!'')?j_string}",
    "title" : "${(item.node.properties.title!'')?j_string}",
	<@addContent item=item />
	"author" : "${item.node.properties.creator?j_string}",
	"createdOn" : "${item.createdDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn" : "${item.modifiedDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"permissions" : {"edit" : true, "publishExt" : true, "delete" : true},
	"commentCount" : ${item.commentCount?c},
	"tags" : <@renderTags tags=item.tags />,
	
	<#-- draft vs internal published -->
	"isDraft" : ${item.isDraft?string},
	<#-- true if the post has been updated -->
	"isUpdated" : ${item.isUpdated?string},
	
	<#-- external publishing -->
	"isPublished" : ${(item.node.properties["blg:published"]!'false')?string},
	<#if (item.node.properties["blg:published"]?? && item.node.properties["blg:published"] == true)>
	"publishedOn" : "${item.node.properties["blg:posted"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"updatedOn" : "${item.node.properties["blg:lastUpdate"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"postId" : "${item.node.properties["blg:postId"]!''}",
	"postLink" : "${item.node.properties["blg:link"]!''}",
	"outOfDate" : ${item.outOfDate?string}
	</#if>
	
}
</#macro>
