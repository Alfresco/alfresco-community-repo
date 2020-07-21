<#import "solr.lib.ftl" as solrLib/>
{
   "diffs" :
   [
      <#list diffs as diff>
          {
             "name": "${diff.modelName}",
             "type" : "${diff.type}",
             "oldChecksum": <#if diff.oldChecksum??>${diff.oldChecksum?c}<#else>null</#if>,
             "newChecksum": <#if diff.newChecksum??>${diff.newChecksum?c}<#else>null</#if>
          }<#if diff_has_next>,</#if>
      </#list>
   ]
}