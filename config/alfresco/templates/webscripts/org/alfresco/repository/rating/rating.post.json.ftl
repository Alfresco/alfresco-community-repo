<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "ratedNodeUrl": "${ratedNode!""}",
      "rating": ${rating?c},
      "ratingScheme": "${ratingScheme!""}"
   }
}
</#escape>