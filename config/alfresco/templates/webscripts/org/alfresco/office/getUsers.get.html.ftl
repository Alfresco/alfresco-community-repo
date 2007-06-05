<#assign n=0>
<#list searchResults as result>
   <#if (n > 0)>,<#else>[</#if>
"${"${result.properties.firstName} ${result.properties.lastName}"?trim}"
   <#assign n=n+1>
</#list>
]