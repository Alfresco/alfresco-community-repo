<#--

	This template renders a comment.

-->
<#macro commentJSON item>
{
	"url" : "comment/node/${item.node.nodeRef?replace('://','/')}",
	"nodeRef" : "${item.node.nodeRef}",
	"name" : "${(item.node.properties.name!'')?j_string}",
    "title" : "${(item.node.properties.title!'')?j_string}",
	"content" : "${item.node.content?j_string}",
	"author" : "${item.node.properties.creator?j_string}",
	"createdOn" : "${item.node.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn" : "${item.node.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"isUpdated" : ${item.isUpdated?string},
	"permissions" : {"edit" : true, "delete" : true}
}
</#macro>

