<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "recordableVersions":
      [
         <#list recordableVersions as recordableVersion>
         {
            "policy": "${recordableVersion.policy}",
            "selected": "${recordableVersion.selected?string("true", "false")}"
         }<#if recordableVersion_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>