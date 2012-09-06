{
   "data":
   {
      "datasets":
      [
         <#list datasets as item>
         {
            "label": "${item.label}",
            "id": "${item.id}"
         }<#if item_has_next>,</#if>
         </#list>
      ]
   }
}