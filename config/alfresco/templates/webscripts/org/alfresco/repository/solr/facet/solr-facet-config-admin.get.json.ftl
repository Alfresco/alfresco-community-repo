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
             "scopedSites" : [
              <#if facet.scopedSites??>
                  <#list facet.scopedSites as site>
                     "${site}"<#if site_has_next>,</#if>
                  </#list>
              </#if>
              ],
             "index" : ${facet.index?c},
             "isEnabled" : ${facet.enabled?c},
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
