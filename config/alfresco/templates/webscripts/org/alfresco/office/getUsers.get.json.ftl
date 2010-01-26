<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list searchResults as result>
   "${"${result.properties.firstName} ${result.properties.lastName}"?trim} (${result.properties.userName})"<#if result_has_next>,</#if>
</#list>
]
</#escape>