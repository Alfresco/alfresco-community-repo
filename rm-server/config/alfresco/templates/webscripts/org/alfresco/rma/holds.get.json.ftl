<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "holds":
      [
         <#list holds as hold>
         {
            "name": "${hold.name}",
            "nodeRef": "${hold.nodeRef}"
         }<#if hold_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>