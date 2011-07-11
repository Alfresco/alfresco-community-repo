<#-- Response to a request to check the authorisation status of a publishing channel -->
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data": 
   {
      "channelId" : "${channelId}",
      "authStatus": "${authStatus}",
   }
}
</#escape>
