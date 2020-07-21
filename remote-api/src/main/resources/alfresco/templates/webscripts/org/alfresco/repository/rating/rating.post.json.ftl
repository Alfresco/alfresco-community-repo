<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "ratedNodeUrl": "${ratedNode!""}",
      "rating": ${rating?c},
      "ratingScheme": "${ratingScheme!""}",
      "averageRating": ${averageRating?c},
      "ratingsTotal": ${ratingsTotal?c},
      "ratingsCount": ${ratingsCount?c}
   }
}
</#escape>