<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items":
   [
   <#list images.items as item>
      {
         "title": "${(item.properties.title!item.name)}",
         "nodeRef": "${item.nodeRef}"
      }<#if item_has_next>,</#if>
   </#list>
   ]
}
</#escape>