{
   "data":
   {
      "holds":
      [
         <#list holds as hold>
         {
            "name": "${hold}"
         }<#if hold_has_next>,</#if>
         </#list>
      ]
   }
}