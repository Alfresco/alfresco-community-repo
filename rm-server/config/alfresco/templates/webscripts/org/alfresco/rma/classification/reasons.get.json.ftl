{
   "data":
   {
      "items":
      [
         <#list reasons as reason>
         {
            "id": "${reason.id?json_string}",
            "displayLabel": "${reason.displayLabel?json_string}",
            "fullReason": "${reason.id?json_string}: ${reason.displayLabel?json_string}"
         }<#if reason_has_next>,</#if>
         </#list>
      ]
   }
}
