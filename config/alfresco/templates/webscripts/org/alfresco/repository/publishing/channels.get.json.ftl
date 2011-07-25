<#-- List Channels -->
<#import "publishing.lib.ftl" as publishLib />
{
   "data": 
   {
      <#if data.urlLength??>"urlLength": ${data.urlLength},</#if>
      "publishChannels":
      <@publishLib.channelsJSON channels=data.publishChannels />,
      "statusUpdateChannels":
      <@publishLib.channelsJSON channels=data.statusUpdateChannels />
   }
}
