<#macro tenantJSON tenant>
      {
         "tenantDomain": "${tenant.tenantDomain}",
         "enabled": "${tenant.enabled?string}",
         "contentRoot": "${tenant.rootContentStoreDir!""}"
      }
</#macro>
