<#-- Response to a request to create a publishing channel -->
<#import "publishing.lib.ftl" as publishLib />
{
   "data": 
   <@publishLib.channelAuthJSON />
}
