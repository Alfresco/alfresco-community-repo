<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "purgedNodes":
      [
         <#list purgedNodes as purgedNode>
         {
            "nodeRef": "${purgedNode}"
         }<#if purgedNode_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>