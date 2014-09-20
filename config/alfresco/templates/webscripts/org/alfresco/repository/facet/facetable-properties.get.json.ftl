<#import "../generic-paged-results.lib.ftl" as genericPaging />
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
                <#if property.propertyDefinition.containerClass.name??>
                "containerClassType" : "${property.propertyDefinition.containerClass.name.prefixString}",
                </#if>
                "dataType" : "${property.propertyDefinition.dataType.name.prefixString}",
                "modelQName" : "${property.propertyDefinition.model.name.prefixString}"
            }<#if property_has_next>,</#if>
            </#list>
        ]
    }
    
   <@genericPaging.pagingJSON />
}
</#escape>
