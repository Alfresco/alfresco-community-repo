<#import "../generic-paged-results.lib.ftl" as genericPaging />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "data" : {
        "properties" : [
            <#list properties as property>
            {
                "name" : "${property.shortQname}"
                <#if property.qname??>
               ,"longqname" : "${property.qname?string}"
                <#else>
               ,"longqname" : "${property.shortQname}"
                </#if>
                <#if property.title??>
               ,"title" : "${property.title}"
                </#if>
               ,"displayName" : "${property.displayName}"
                <#if property.containerClassType??>
               ,"containerClassType" : "${property.containerClassType.prefixString}"
                </#if>
                <#if property.dataType??>
               ,"dataType" : "${property.dataType.prefixString}"
                </#if>
                <#if property.modelQname??>
               ,"modelQName" : "${property.modelQname.prefixString}"
                </#if>
            }<#if property_has_next>,</#if>
            </#list>
        ]
    }
    
   <@genericPaging.pagingJSON />
}
</#escape>
