<#-- List Channels -->
<#import "publishing.lib.ftl" as publishLib />
{
   "data": 
   [
      <#if data??>
      <#list data as type>
      <@publishLib.channelTypeJSON type=type/>
      <#if type_has_next>,</#if>
      </#list>
      </#if>
   ]
}
