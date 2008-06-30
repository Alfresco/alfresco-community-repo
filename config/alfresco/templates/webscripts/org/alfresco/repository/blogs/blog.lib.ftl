<#--
	This template renders the blog configuration json data.
-->
<#macro blogJSON item>
{
    "qnamePath" : "${item.qnamePath}",
	"url" : "blog/node/${item.nodeRef?replace('://', '/')}",
	"blogPostsUrl" : "blog/node/${item.nodeRef?replace('://', '/')}/posts",
	"type" : "${item.properties["blg:blogImplementation"]!''}",
	"id" : "${item.properties["blg:id"]!'0'}",
	"name" : "${(item.properties["blg:name"]!'')?j_string}",
    "description" : "${(item.properties["blg:description"]!'')?j_string}",
	"url" : "${item.properties["blg:url"]!''}",
	"username" : "${item.properties["blg:userName"]!''}",
	"password" : "${item.properties["blg:password"]!''}",
}
</#macro>
