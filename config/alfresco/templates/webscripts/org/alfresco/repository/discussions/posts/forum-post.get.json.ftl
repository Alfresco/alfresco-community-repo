{
	<#if topicpost??>
		<#import "../topicpost.lib.ftl" as topicpostLib />
		"item": <@topicpostLib.topicpostJSON item=topicpost />
	<#else>
		<#import "../post.lib.ftl" as postLib />
		"item": <@postLib.postJSON post=post />
	</#if>
}