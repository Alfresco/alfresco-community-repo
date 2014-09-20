<#import "../generic-paged-results.lib.ftl" as genericPaging />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "data" : {
        "properties" : [
            <#list properties as property>
            {
                "name" : "${property.shortQname}",
                "longqname" : "${property.qname?string}",
                <#if property.title??>
                "title" : "${property.title}",
                </#if>
                "displayName" : "${property.displayName}",
                <#if property.containerClassType??>
                "containerClassType" : "${property.containerClassType.prefixString}",
                </#if>
                "dataType" : "${property.dataType.prefixString}",
                "modelQName" : "${property.modelQname.prefixString}"
            }<#if property_has_next>,</#if>
            </#list>
        ]
    }
    
   <@genericPaging.pagingJSON />
}
</#escape>
