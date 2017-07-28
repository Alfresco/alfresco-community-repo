<#import "tenant.lib.ftl" as tenantLib/>
{ 
   "tenants" :
   [
      <#list tenants as tenant>
         <@tenantLib.tenantJSON tenant=tenant/>
         <#if tenant_has_next>,</#if>
      </#list>
   ]
}