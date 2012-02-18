<#import "solr.lib.ftl" as solrLib/>
{ 
   "transactions" :
   [
      <#list transactions as txn>
         <@solrLib.transactionJSON txn=txn/>
         <#if txn_has_next>,</#if>
      </#list>
   ]
   <#if maxTxnCommitTime??>
    ,"maxTxnCommitTime": ${maxTxnCommitTime?c}
   </#if>
   <#if maxTxnId??>
    ,"maxTxnId": ${maxTxnId?c}
   </#if>
   
}