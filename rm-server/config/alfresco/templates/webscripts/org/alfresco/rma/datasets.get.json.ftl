{
   "data":
   {
      "datasets":
      [
         <#list datasets as item>
         {
            "label": "${item.label}",
            "id": "${item.id}",
            "isLoaded": "${item.isLoaded}"
         }<#if item_has_next>,</#if>
         </#list>
      ]
   }
}