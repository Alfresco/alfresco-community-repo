<#escape x as jsonUtils.encodeJSONString(x)>
{
    "properties" : {
        <#list properties as property>
        "${property.second.name.prefixString}" : {
             <#if property.first??>
             "title" : "${property.first}",
             </#if>
             "dataType" : "${property.second.dataType.name.prefixString}",
             "modelQName" : "${property.second.model.name.prefixString}"
        }<#if property_has_next>,</#if>
        </#list>
    }
}
</#escape>
