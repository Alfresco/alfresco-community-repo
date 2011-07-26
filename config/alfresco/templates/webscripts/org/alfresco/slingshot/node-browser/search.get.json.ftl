{
   "numResults": ${results?size},
   "results": [
   <#list results as result>
      {
         "name": "${result.name}",
         "path": "${result.qnamePath}",
         "parentPath": "${result.qnamePath?substring(0, result.qnamePath?last_index_of('/'))}",
         "parentNodeRef": "${result.parent.nodeRef}",
         "nodeRef": "${result.nodeRef}"
      }<#if result_has_next>,</#if>
   </#list>
   ]
}