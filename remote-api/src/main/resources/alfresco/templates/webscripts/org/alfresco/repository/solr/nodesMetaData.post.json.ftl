<#import "solr.lib.ftl" as solrLib/>
{
   "nodes" :
   [
      <#list nodes as nodeMetaData>
         <@solrLib.nodeMetaDataJSON nodeMetaData=nodeMetaData filter=filter/><#if nodeMetaData_has_next>,</#if>
      </#list>
   ]
}