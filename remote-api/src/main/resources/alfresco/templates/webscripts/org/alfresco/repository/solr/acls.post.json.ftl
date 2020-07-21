<#import "solr.lib.ftl" as solrLib/>
{ 
   "acls" :
   [
      <#list acls as acl>
         <@solrLib.aclJSON acl=acl/>
         <#if acl_has_next>,</#if>
      </#list>
   ]
}