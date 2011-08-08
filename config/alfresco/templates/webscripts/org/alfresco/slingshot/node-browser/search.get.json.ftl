{
   "numResults": ${results?size},
   "results": [
   <#list results as result>
      {
         "name": "${result.name}",
         "path": "${result.qnamePath}",
         "parentPath": "${result.qnamePath?substring(0, result.qnamePath?last_index_of('/'))}",
         "parentNodeRef": <#if result.parent??>null<#else>"${result.parent.nodeRef}"</#if>,
         "nodeRef": "${result.nodeRef}"
      }<#if result_has_next>,</#if>
   </#list>
   ]
}