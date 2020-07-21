<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "nodeRef": "${nodeRef}",
      "ratings":
      {
         <#list ratings as rating>
         "${rating.scheme.name!""}":
         {
            "rating": ${rating.score?c},
            "appliedAt": "${xmldate(rating.appliedAt)}",
            "appliedBy": "${rating.appliedBy!""}"
         }<#if rating_has_next>,</#if>
         </#list>
      },
      "nodeStatistics":
      {
         <#list averageRatings?keys as schemeName>
         "${schemeName!""}":
         {
            "averageRating": ${averageRatings[schemeName]?c},
            "ratingsTotal": ${ratingsTotals[schemeName]?c},
            "ratingsCount": ${ratingsCounts[schemeName]?c}
         }<#if schemeName_has_next>,</#if>
         </#list>
      }
   }
}
</#escape>