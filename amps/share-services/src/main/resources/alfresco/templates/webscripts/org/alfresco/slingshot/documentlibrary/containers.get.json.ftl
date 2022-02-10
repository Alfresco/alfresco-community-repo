<#escape x as jsonUtils.encodeJSONString(x)>
{
   "containers":
   [
   <#list containers as container>
      {
         "name": "${container.name}",
         "description": "${container.properties.description!"Document Library"}",
         "nodeRef": "${container.nodeRef}",
         "type": "${container.typeShort}"
      }<#if container_has_next>,</#if>
   </#list>
   ]
}
</#escape>
