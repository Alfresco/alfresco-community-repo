<#import "solr.lib.ftl" as solrLib/>
{ 
   "aclChangeSets" :
   [
      <#list aclChangeSets as aclChangeSet>
         <@solrLib.aclChangeSetJSON aclChangeSet=aclChangeSet/>
         <#if aclChangeSet_has_next>,</#if>
      </#list>
   ]
}