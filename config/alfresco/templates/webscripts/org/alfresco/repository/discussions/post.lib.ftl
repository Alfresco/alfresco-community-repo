<#-- Renders a person object. -->
<#macro renderPerson person fieldName>
<#escape x as jsonUtils.encodeJSONString(x)>
	"${fieldName}":
	{
		<#if person.assocs["cm:avatar"]??>
		"avatarRef": "${person.assocs["cm:avatar"][0].nodeRef?string}",
		</#if>
		"username": "${person.properties["cm:userName"]}",
		"firstName": "${person.properties["cm:firstName"]!""}",
		"lastName": "${person.properties["cm:lastName"]!""}"
	},
</#escape>
</#macro>


<#macro addContent post>
<#escape x as jsonUtils.encodeJSONString(x)>
   <#assign safecontent=stringUtils.stripUnsafeHTML(post.content)>
	<#if (contentLength?? && contentLength > -1 && (safecontent?length > contentLength))>
		"content": "${safecontent?substring(0, contentLength)}",
	<#else>
		"content": "${safecontent}",
	</#if>
</#escape>
</#macro>


<#macro postJSON postData>
{
	<@postDataJSON postData=postData />
}
</#macro>

<#macro postDataJSON postData>
<#escape x as jsonUtils.encodeJSONString(x)>
	<#-- which node should be used for urls? which for the post data? -->
	<#if postData.isTopicPost>
		<#assign refNode=postData.topic />
	<#else>
		<#assign refNode=postData.post />
	</#if>
	
	<#assign post=postData.post />

	<#-- render topic post only data first -->
	<#if postData.isTopicPost>
		"name": "${postData.topic.name}",
		"totalReplyCount": ${postData.totalReplyCount?c},
		<#if postData.lastReply??>
			"lastReplyOn": "${postData.lastReply.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
			<#if postData.lastReplyBy??>
			<@renderPerson person=postData.lastReplyBy fieldName="lastReplyBy" />
         <#else>
         "lastReplyBy":
         {
            "username": ""
         },
         </#if>
		</#if>
		"tags": [<#list postData.tags as x>"${x}"<#if x_has_next>, </#if></#list>],
	</#if>

	<#-- data using refNode which might be the topic or the post node -->
	"url": "/forum/post/node/${refNode.nodeRef.storeRef.protocol}/${refNode.nodeRef.storeRef.identifier}/${refNode.nodeRef.id}",
	"repliesUrl": "/forum/post/node/${refNode.nodeRef.storeRef.protocol}/${refNode.nodeRef.storeRef.identifier}/${refNode.nodeRef.id}/replies",
	"nodeRef": "${refNode.nodeRef}",
	
	<#-- data coming from the post node -->
	"title": "${(post.properties.title!"")}",
	"createdOn": "${post.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn": "${post.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	<#if (post.properties["cm:updated"]??)>
	"isUpdated": true,
	"updatedOn": "${post.properties["cm:updated"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	<#else>
	"isUpdated": false,   
	</#if>
	<#if postData.author??>
		<@renderPerson person=postData.author fieldName="author" />
	<#else>
		"author":
		{
			"username": "${post.properties["cm:creator"]}"
		},
	</#if>
	<@addContent post=post />
	"replyCount": <#if post.sourceAssocs["cm:references"]??>${post.sourceAssocs["cm:references"]?size?c}<#else>0</#if>,
	"permissions":
	{
		"edit": ${postData.canEdit?string},
		"reply": ${post.parent.hasPermission("CreateChildren")?string},
		"delete": ${post.hasPermission("Delete")?string}
	}
</#escape>
</#macro>


<#-- Renders replies.
	The difference is to a normal post is that the children might be
	added inline in the returned JSON.
-->
<#macro repliesJSON data>
{
	<@postDataJSON postData=data />
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