<#macro listJSON list>
   <#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${list.name}",
   "title": "${list.properties.title!list.name}",
   "description": "${list.properties.description!""}",
   "nodeRef": "${list.nodeRef}",
   "itemType": "${list.properties["dl:dataListItemType"]!""}",
   "permissions":
   {
      "edit": ${list.hasPermission("Write")?string},
      "delete": ${list.hasPermission("Delete")?string}
   }
}
   </#escape>
</#macro>