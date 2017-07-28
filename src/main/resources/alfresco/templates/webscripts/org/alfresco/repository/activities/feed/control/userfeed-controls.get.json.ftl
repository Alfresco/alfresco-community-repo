<#escape x as jsonUtils.encodeJSONString(x)>
[
<#if feedControls??>
   <#list feedControls as feedControl>
   { 
      "siteId": "${feedControl.siteId}",
      "appToolId": "${feedControl.appToolId}"
   }<#if feedControl_has_next>,</#if>
   </#list>
</#if>
]
</#escape>