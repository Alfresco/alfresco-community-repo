<#macro facetJSON facet>
<#escape x as jsonUtils.encodeJSONString(x)>
             "filterID" : "${facet.filterID}",
             "facetQName" : "${facet.facetQName}",
             "displayName" : "${facet.displayName}",
             "displayControl" : "${facet.displayControl}",
             "maxFilters" : ${facet.maxFilters?c},
             "hitThreshold" : ${facet.hitThreshold?c},
             "minFilterValueLength" : ${facet.minFilterValueLength?c},
             "sortBy" : "${facet.sortBy}",
             "scope" : "${facet.scope}",
             <#if facet.scopedSites?size != 0>
             "scopedSites" : [
                  <#list facet.scopedSites as site>
                     "${site}"<#if site_has_next>,</#if>
                  </#list>
             ],
             </#if>
             <#if facet.customProperties?size != 0>
             "customProperties" : 
             {
                <#list facet.customProperties as propDetails>
                "${propDetails.name.localName?string}":
                {
                    "name" : "${propDetails.name?string}",
                    <#if propDetails.value?is_enumerable>
                    "value" : [
                    <#list propDetails.value as v>
                    "${v?string}"<#if v_has_next>,</#if>
                    </#list>
                    ]
                    <#else>
                    "value" : "${propDetails.value?string}"
                    </#if>
                }<#if propDetails_has_next>,</#if>
                </#list>
             },
             </#if>
             "isEnabled" : ${facet.isEnabled()?c},
             "isDefault" : ${facet.default?c}
</#escape>
</#macro>

{
   <#if filters??>
    "facets" : [
           <#list filters as facet>
           {
               <@facetJSON facet=facet />
           }<#if facet_has_next>,</#if>
           </#list>
          ]

   <#else>
    <@facetJSON facet=filter />
   </#if>
}
