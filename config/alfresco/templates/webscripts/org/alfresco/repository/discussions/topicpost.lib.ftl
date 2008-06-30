<#import "post.lib.ftl" as postLib/>

<#--

	This template renders a post.

-->
<#macro topicpostJSON item>
{
	"name" : "${item.topic.name?js_string}",
	"totalReplyCount" : ${item.totalReplyCount?c},
	<#if item.lastReply??>
	"lastReplyBy" : "${item.lastReply.properties.creator}",
	"lastReplyOn" : "${item.lastReply.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	</#if>
	<@postLib.postDataJSON refNode=item.topic post=item.post />
}
</#macro>