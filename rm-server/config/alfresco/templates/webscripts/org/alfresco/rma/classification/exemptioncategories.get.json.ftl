{
   "data":
   {
      "items":
      [
         <#list exemptionCategories as exemptionCategory>
         {
            "id": "${exemptionCategory.id?json_string}",
            "displayLabel": "${exemptionCategory.displayLabel?json_string}",
            "fullCategory": "${exemptionCategory.id?json_string}: ${exemptionCategory.displayLabel?json_string}"
         }<#if exemptionCategory_has_next>,</#if>
         </#list>
      ]
   }
}
