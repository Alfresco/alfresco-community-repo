<#--
   This template renders the blog data object.
-->
<#macro blogJSON item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "qnamePath": "${item.qnamePath}",
   "detailsUrl": "blog/node/${item.nodeRef?replace('://', '/')}",
   "blogPostsUrl": "blog/node/${item.nodeRef?replace('://', '/')}/posts",
   "type": "${item.properties["blg:blogImplementation"]!''}",
   "id": "${item.properties["blg:id"]!'0'}",
   "name": "${item.properties["blg:name"]!''}",
   "description": "${item.properties["blg:description"]!''}",
   "url": "${item.properties["blg:url"]!''}",
   "username": "${item.properties["blg:userName"]!''}",
   "password": "${item.properties["blg:password"]!''}",
   "permissions":
   {
      "create": ${item.hasPermission("CreateChildren")?string},
      "edit": ${item.hasPermission("Write")?string},
      "delete": ${item.hasPermission("Delete")?string}
   }
}
</#escape>
</#macro>
