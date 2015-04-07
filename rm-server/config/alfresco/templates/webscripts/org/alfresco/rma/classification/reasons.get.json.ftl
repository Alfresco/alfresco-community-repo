{
   "data":
   {
      "items":
      [
         <#list reasons as reason>
         {
            "id": "${reason.id?json_string}",
            "displayLabel": "${reason.displayLabel?json_string}"
         }<#if reason_has_next>,</#if>
         </#list>
      ]
   }
}
