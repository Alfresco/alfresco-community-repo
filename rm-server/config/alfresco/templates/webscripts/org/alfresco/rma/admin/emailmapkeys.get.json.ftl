<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data" :
   {
      "emailmapkeys":
      [
         <#list emailmapkeys as emailmapkey>
            '${emailmapkey}'<#if emailmapkey_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>