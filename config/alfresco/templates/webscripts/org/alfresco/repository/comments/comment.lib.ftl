<#-- Renders a person object. -->
<#macro renderPerson person fieldName>
   "${fieldName}" : {
      <#if person.assocs["cm:avatar"]??>
      "avatarRef" : "${person.assocs["cm:avatar"][0].nodeRef?string}",
      </#if>
      "username" : "${person.properties["cm:userName"]}",
      "firstName" : "${person.properties["cm:firstName"]}",
      "lastName" : "${person.properties["cm:lastName"]}"
   },
</#macro>

<#--

	This template renders a comment.

-->
<#macro commentJSON item>
{
	"url" : "api/comment/node/${item.node.nodeRef?replace('://','/')}",
	"nodeRef" : "${item.node.nodeRef}",
	"name" : "${(item.node.properties.name!'')?j_string}",
    "title" : "${(item.node.properties.title!'')?j_string}",
	"content" : "${item.node.content?j_string}",
   <#if item.author??>
   <@renderPerson person=item.author fieldName="author" />
   <#else>
   "author" : { "username" : "${item.node.properties.creator?j_string}" },
   </#if>
	"createdOn" : "${item.node.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn" : "${item.node.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"isUpdated" : ${item.isUpdated?string},
	"permissions" : {"edit" : true, "delete" : true}
}
</#macro>

