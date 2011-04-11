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
            "nodeType": "${deletedNode.nodeType!""}"
         }<#if deletedNode_has_next>,</#if>
         </#list>
      ]
   }
   <#if paging??>,
   "paging": 
   {
      "totalItems": ${paging.totalItems?c},
      "maxItems": ${paging.maxItems?c},
      "skipCount": ${paging.skipCount?c}
   }
   </#if>
}
</#escape>