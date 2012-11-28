<#import "emailmap.lib.ftl" as emailmapLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
   "success": ${success?string},
   <#if success>
      "data": <@emailmapLib.emailmapJSON emailmap=emailmap />
   <#else>
      "message": "${message}"
   </#if>
}
</#escape>