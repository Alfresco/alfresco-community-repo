<#import "application.lib.ftl" as auditApplicationLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "enabled" : ${enabled?string("true","false")},
   "applications": 
   [
      <#list applications as application>
         <@auditApplicationLib.auditApplicationJSON auditApplication=application />
         <#if application_has_next>,</#if>
      </#list>
   ]
}
</#escape>