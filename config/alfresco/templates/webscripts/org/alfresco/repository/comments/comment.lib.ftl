<#--

	This template renders a comment.

-->
<#macro commentJSON item>
{
	"url" : "comment/node/${item.nodeRef?replace('://','/')}",
	"nodeRef" : "${item.nodeRef}",
	"name" : "${(item.properties.name!'')?j_string}",
    "title" : "${(item.properties.title!'')?j_string}",
	"content" : "${item.content?j_string}",
	"author" : "${item.properties.creator?j_string}",
	"createdOn" : "${item.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn" : "${item.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"permissions" : {"edit" : true, "delete" : true}
}
</#macro>

