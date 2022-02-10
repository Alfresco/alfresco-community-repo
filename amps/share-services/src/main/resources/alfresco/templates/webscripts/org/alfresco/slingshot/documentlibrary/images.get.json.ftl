<#macro dateFormat date>${xmldate(date)}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items":
   [
   <#list images.items as item>
      {
         "name": "${(item.properties.name)}",
         "title": "${(item.properties.title!item.name)}",
         "modifier": "${(item.properties.modifier)}",
         "modifiedOn": "<@dateFormat item.properties.modified />",
         "nodeRef": "${item.nodeRef}"
      }<#if item_has_next>,</#if>
   </#list>
   ]
}
</#escape>