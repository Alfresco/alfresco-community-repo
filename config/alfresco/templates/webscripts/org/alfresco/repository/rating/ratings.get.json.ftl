<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "nodeRef": "${nodeRef}",
      "ratings":
      [
         <#list ratings as rating>
         {
            "ratingScheme": "${rating.scheme.name!""}",
            "rating": ${rating.score?c},
            "appliedAt": "<@dateFormat rating.appliedAt />",
            "appliedBy": "${rating.appliedBy!""}"
         }<#if rating_has_next>,</#if>
         </#list>
      ],
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