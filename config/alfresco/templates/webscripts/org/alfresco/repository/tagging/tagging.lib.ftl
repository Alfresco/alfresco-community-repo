<#macro tagJSON item>
{
   "name" : "${jsonUtils.encodeJSONString(item.name)}",
   "count" : ${item.count?c}
}</#macro>
