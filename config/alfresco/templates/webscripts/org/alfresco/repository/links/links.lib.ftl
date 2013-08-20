<#import "../generic-paged-results.lib.ftl" as gen>

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

<#--
   This template renders a link.
-->
<#macro linkJSON item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "url": "${item.url!''}",
   "commentsUrl": "/node/${item.node.nodeRef?replace('://','/')}/comments",
   "description": "${item.description!''}",
   "nodeRef": "${item.node.nodeRef}",
   "name": "${item.name!''}",
   "title": "${item.title!''}",
   "internal": ${(item.internal!false)?string},
   "createdOn": "${xmldate(item.createdOn)}",
   "createdOnDate": {
        "iso8601": "${xmldate(item.createdOn)}",
        "legacyDate": "${xmldate(item.createdOn)}"
   },
   <#if item.creator?has_content>
   <@renderPerson person=item.creator fieldName="author" />
   <#else>
   "author":
   {
      "username": "${item.node.properties.creator}"
   },
   </#if>
   "permissions":
   {
      "edit": ${item.node.hasPermission("Write")?string},
      "delete": ${item.node.hasPermission("Delete")?string}
   },
   "tags": [<#list item.tags as x>"${x}"<#if x_has_next>, </#if></#list>]
}
</#escape>
</#macro>

<#macro renderLinkList>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "metadata":
   {
      "linkPermissions":
      {
         "create": "${links.hasPermission("CreateChildren")?string}"
      }
   },
   "totalRecordsUpper": ${data.totalRecordsUpper?string("true","false")},
   <@gen.pagedResults data=data ; item>
      <@linkJSON item=item />
   </@gen.pagedResults>
}
</#escape>
</#macro>

<#macro renderLink>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "item": <@linkJSON item=item />
}
</#escape>
</#macro>
