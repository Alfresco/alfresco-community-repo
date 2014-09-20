<#escape x as jsonUtils.encodeJSONString(x)>
{
    "properties" : {
        <#list properties as property>
        "${property.name}" : {
             "modelQName" : "${property.model.name.prefixString}",
             "dataType" : "${property.dataType.name.prefixString}"
        }<#if property_has_next>,</#if>
        </#list>
    }
}
</#escape>
