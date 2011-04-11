<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "nodeRef": "${nodeRef}",
      "averageRating": ${averageRating?c},
      "ratingsTotal": ${ratingsTotal?c},
      "ratingsCount": ${ratingsCount?c}
   }
}
</#escape>