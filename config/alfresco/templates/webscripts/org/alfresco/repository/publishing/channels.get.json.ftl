<#-- List Channels -->
<#import "publishing.lib.ftl" as publishLib />
{
   "data": 
   {
      urlLength: ${data.urlLength},
      "publishChannels":
      <@publishLib.channelsJSON channels=data.publishChannels />,
      "statusUpdateChannels":
      <@publishLib.channelsJSON channels=data.statusUpdateChannels />
   }
}
