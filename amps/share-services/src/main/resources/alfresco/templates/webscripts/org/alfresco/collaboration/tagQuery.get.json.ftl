<#escape x as jsonUtils.encodeJSONString(x)>
{
   "countMin": ${tagQuery.countMin?c},
   "countMax": ${tagQuery.countMax?c},
   "tags": 
   [<#list tagQuery.tags as tag>
      { "name": "${tag.name}", "count": ${tag.count?c} }<#if tag_has_next>,</#if>
   </#list>]
}
</#escape>