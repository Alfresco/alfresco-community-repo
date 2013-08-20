<#macro renderItem item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRef": "${item.nodeRef}",
   "widgetType": "${item.widgetType!''}",
   "mid": "${item.mid!''}",
   "label": "${item.label!''}"
}
</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "widgets": 
      [
      <#list data as item>
         <@renderItem item /><#if item_has_next>,</#if>
      </#list>
      ]
}
</#escape>