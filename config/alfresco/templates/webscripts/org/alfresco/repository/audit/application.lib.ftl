<#-- Renders an Audit Application. -->
<#macro auditApplicationJSON auditApplication>
      {
         "name": "${auditApplication.name}",
         "path" : "${auditApplication.key}",
         "enabled" : ${auditApplication.enabled?string("true","false")}
      }
</#macro>
