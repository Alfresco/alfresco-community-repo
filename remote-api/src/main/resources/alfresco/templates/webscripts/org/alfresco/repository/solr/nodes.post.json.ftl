<#import "solr.lib.ftl" as solrLib/>
{
   "nodes" :
   [
      <#list nodes as node>
         <@solrLib.nodeJSON node=node/>
         <#if node_has_next>,</#if>
      </#list>
   ]
}