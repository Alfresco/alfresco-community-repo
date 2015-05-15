{
   "data":
   {
      "items":
      [
         <#list levels as level>
         {
            "id": "${level.highestClassificationLevel.id?json_string}",
            "displayLabel": "${level.displayLabel?json_string}"
         }<#if level_has_next>,</#if>
         </#list>
      ]
   }
}
