<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "items":
      [
         <#list reasons as reason>
         {
            "id": "<#noescape>${reason.id}</#noescape>",
            "displayLabel": "<#noescape>${reason.displayLabel}</#noescape>"
         }<#if reason_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>