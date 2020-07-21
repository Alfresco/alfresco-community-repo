<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "ratingSchemes":
      [
         <#list schemeDefs?keys as key>
         {
            "name": "${schemeDefs[key].name!""}",
            "minRating": ${schemeDefs[key].minRating?c},
            "maxRating": ${schemeDefs[key].maxRating?c},
            "selfRatingAllowed": ${schemeDefs[key].selfRatingAllowed?string}
         }<#if key_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>