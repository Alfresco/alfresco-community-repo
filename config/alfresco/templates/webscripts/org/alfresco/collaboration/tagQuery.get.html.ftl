{
"countMin": "${tagQuery.countMin}",
"countMax": "${tagQuery.countMax}",
"tags": 
<#assign n=0>
<#list tagQuery.tags as tag>
   <#if (n > 0)>,<#else>[</#if>
{"name": "${tag.name}", "count": "${tag.count}"}
   <#assign n=n+1>
</#list>
]
}