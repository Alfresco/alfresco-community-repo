<#import "../generic-paged-results.lib.ftl" as genericPaging />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "deletedNodes":
      [
         <#list deletedNodes as deletedNode>
         {
            "nodeRef": "${deletedNode.nodeRef}",
            "archivedBy": "${deletedNode.archivedBy!""}",
            "archivedDate": "${xmldate(deletedNode.archivedDate)}",
            "name": "${deletedNode.name!""}",
            "title": "${deletedNode.title!""}",
            "description": "${deletedNode.description!""}",
            "displayPath": "${deletedNode.displayPath!""}",
            "firstName": "${deletedNode.firstName!""}",
            "lastName": "${deletedNode.lastName!""}",
            "nodeType": "${deletedNode.nodeType!""}",
            "isContentType": ${deletedNode.isContentType?string}
         }<#if deletedNode_has_next>,</#if>
         </#list>
      ]
   }

   <@genericPaging.pagingJSON />
}
</#escape>
