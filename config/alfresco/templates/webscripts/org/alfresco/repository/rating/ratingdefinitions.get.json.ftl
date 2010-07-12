<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "ratingSchemes":
      [
         <#list schemeDefs?keys as key>
         {
            "name": "${schemeDefs[key].name!""}",
            "minRating": ${schemeDefs[key].minRating},
            "maxRating": ${schemeDefs[key].maxRating}
         }<#if key_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>