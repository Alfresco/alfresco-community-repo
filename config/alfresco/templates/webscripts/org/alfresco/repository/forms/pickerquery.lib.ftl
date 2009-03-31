<#macro pickerQueryJSON results>
    <#escape x as jsonUtils.encodeJSONString(x)>
{
    "data" :
    {
        "items" : 
        [
            <#list results as row>
            {
                "name" : "${row.properties.name}",
                "description" : "${row.displayPath}",
                "nodeRef" : "${row.nodeRef}"
            }<#if row_has_next>,</#if>
            </#list>
        ]
    }
}
	</#escape>
</#macro>