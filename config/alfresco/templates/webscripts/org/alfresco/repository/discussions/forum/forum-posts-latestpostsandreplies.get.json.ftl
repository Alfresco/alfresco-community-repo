<#import "../post.lib.ftl" as postLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>

<#macro latestPostJSON item>
{
	"topicTitle" : "${item.post.properties.title?js_string}",
	<@postLib.postDataJSON refNode=item.topic post=item.reply />
}
</#macro>

<@gen.pagedResults data=data ; item>
	<@latestPostJSON item=item />
</@gen.pagedResults>