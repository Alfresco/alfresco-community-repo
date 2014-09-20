<#escape x as jsonUtils.encodeJSONString(x)>
{
    "data" : {
        "properties" : [
            <#list properties as property>
            {
                "name" : "${property.propertyDefinition.name.prefixString}",
                "longqname" : "${property.propertyDefinition.name?string}",
                <#if property.localisedTitle??>
                "title" : "${property.localisedTitle}",
                </#if>
                "displayName" : "${property.displayName}",
                "dataType" : "${property.propertyDefinition.dataType.name.prefixString}",
                "modelQName" : "${property.propertyDefinition.model.name.prefixString}"
            }<#if property_has_next>,</#if>
            </#list>
        ]
    }
}
</#escape>
