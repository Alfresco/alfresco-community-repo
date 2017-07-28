<#import "solr.lib.ftl" as solrLib/>
{ 
   "aclsReaders" :
   [
      <#list aclsReaders as aclReaders>
         <@solrLib.aclReadersJSON aclReaders=aclReaders/>
         <#if aclReaders_has_next>,</#if>
      </#list>
   ]
}