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
            "rating": ${rating.score},
            "appliedAt": "${rating.appliedAt}",
            "appliedBy": "${rating.appliedBy}"
         }<#if rating_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>