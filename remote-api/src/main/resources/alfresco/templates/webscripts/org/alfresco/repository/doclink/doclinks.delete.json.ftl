<#escape x as jsonUtils.encodeJSONString(x)>
{
	"found": "${total_count}",
	"deleted": "${deleted_count}",
	"error details" : 
   [
      <#list error_details?keys as key>
           "${key} - ${error_details[key]}" 
		   <#if key_has_next>,</#if>
      </#list>
   ]
}
</#escape>