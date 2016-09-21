<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "items":
      [
         <#list relationships as relationship>
         {
            "node": <#noescape>${relationship}</#noescape>
         }<#if relationship_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>