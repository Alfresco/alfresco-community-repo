<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "nodeRef": "${nodeRef}",
      "ratingScheme": "${rating.scheme.name!""}",
      "rating": ${rating.score?c},
      "appliedAt": "<@dateFormat rating.appliedAt />",
      "appliedBy": "${rating.appliedBy!""}"
   }
}
</#escape>