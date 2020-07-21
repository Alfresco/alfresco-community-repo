<#macro renderItem item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRef": "${item.nodeRef}",
   "name": "${item.name!''}",
   "data": "${item.data!''}"
}
</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items": 
      [
      <#list data as item>
         <@renderItem item /><#if item_has_next>,</#if>
      </#list>
      ]
}
</#escape>