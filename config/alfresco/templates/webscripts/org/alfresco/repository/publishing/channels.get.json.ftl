<#-- List Channels -->
<#import "publishing.lib.ftl" as publishLib />
{
   "data": 
   [
      <#list data as channel>
      <@publishLib.channelJSON channel=channel />
      <#if channel_has_next>,</#if>
      </#list>
   ]
}
