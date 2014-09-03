<#escape x as jsonUtils.encodeJSONString(x)>
{
    "facets": [
          <#list filters?keys as facet>
          <#assign f=filters[facet]>
          {
             "filterID" : "${f.filterID}",
             "facetQName" : "${f.facetQName}",
             "displayName" : "${f.displayName}",
             "maxFilters" : ${f.maxFilters?c},
             "hitThreshold" : ${f.hitThreshold?c},
             "minFilterValueLength" : ${f.minFilterValueLength?c},
             "sortBy" : "${f.sortBy}",
             "scope" : "${f.scope}",
             "scopedSites" : [
              <#if f.scopedSites??>
                  <#list f.scopedSites as site>
                     "${site}"<#if site_has_next>,</#if>
                  </#list>
              </#if>
              ],
             "index" : ${f.index?c},
             "isEnabled" : ${f.enabled?c}
          }<#if facet_has_next>,</#if>
          </#list>
    ]
}
</#escape>
