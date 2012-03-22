{
    "message" : "${message}"
<#if result?exists>
    ,"result" : "${result?string}"
</#if>
<#if results?exists>
    ,"results" : 
    {    
    <#list results?keys as prop>
    	"${prop}" : "${results[prop]}"<#if prop_has_next>,</#if> 		
    </#list>
    }
</#if>
}