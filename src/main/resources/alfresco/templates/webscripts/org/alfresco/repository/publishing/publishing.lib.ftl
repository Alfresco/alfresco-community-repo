<#-- Renders a List of Publishing Events -->
<#macro eventsJSON events>
<#escape x as jsonUtils.encodeJSONString(x)>
[
   <#if events??>
   <#list events as event>
   <@eventJSON event=event />
   <#if event_has_next>,</#if>
   </#list>
   </#if>
]
</#escape>
</#macro>


<#-- Renders a Publishing Event. -->
<#macro eventJSON event>
{
   "id": "${event.id}",
   "url": "${event.url}",
   "status": "${event.status}",
   <#if event.comment?? >"comment": "${event.comment}",</#if>
   <#if event.eventType?? >"eventType": "${event.eventType}",</#if>
   <#if event.scheduledTime?? >
   "scheduledTime":
   <@calendarJSON calendar=event.scheduledTime />,
   </#if>
   "creator": "${event.creator}",
   "createdTime":
   <@dateJSON date=event.createdTime/>,
   "publishNodes":
   <@publishNodesJSON nodes=event.publishNodes/>,
   "unpublishNodes":
   <@publishNodesJSON nodes=event.unpublishNodes/>,
   <#if event.channel?? >
   "channel":
   <@channelJSON channel=event.channel/>
   <#elseif event.channelId?? >
   "channelId": "${event.channelId}"
   </#if>
}
</#macro>

<#-- Renders a List of Nodes to be published/unpublished. -->
<#macro publishNodesJSON nodes>
[
   <#if nodes??>
   <#list nodes as node>
   <@publishNodeJSON node=node/>
   <#if node_has_next>,</#if>
   </#list>
   </#if>
]
</#macro>

<#-- Renders a Published/Unpublished Node. -->
<#macro publishNodeJSON node>
{
   <#if node.name?? >"name": "${node.name}",</#if>
   <#if node.version?? >"version": "${node.version}",</#if>
   "nodeRef": "${node.nodeRef}"
}
</#macro>

<#-- Renders a List of Channel.s -->
<#macro channelsJSON channels>
<#escape x as jsonUtils.encodeJSONString(x)>
[
   <#if channels??>
   <#list channels as channel>
   <@channelJSON channel=channel />
   <#if channel_has_next>,</#if>
   </#list>
   </#if>
]
</#escape>
</#macro>

<#-- Renders a Channel. -->
<#macro channelJSON channel>
{
   "url": "${channel.url}",
   "id": "${channel.id}",
   "name": "${channel.name}",
   "title": "${channel.title}",
   "authorised": ${channel.authorised},
   "canPublish": ${channel.canPublish},
   "canPublishStatusUpdates": ${channel.canPublishStatusUpdates},
   "canUnpublish": ${channel.canUnpublish},
   "channelType":
   <@channelTypeJSON type=channel.channelType />
}
</#macro>

<#-- Renders a Channel Type. -->
<#macro channelTypeJSON type>
{
   "id": "${type.id}",
   "title": "${type.title}",
   "url": "${type.url}",
   <#if type.channelNodeType??>"channelNodeType": "${type.channelNodeType}",</#if>
   <#if type.supportedContentTypes?? >
   "supportedContentTypes":
   <@iterateStringsJSON strings=type.supportedContentTypes />,
   </#if>
   <#if type.supportedMimeTypes?? >
   "supportedMimeTypes":
   <@iterateStringsJSON strings=type.supportedMimeTypes/>,
   </#if>
   "canPublish": ${type.canPublish},
   "canPublishStatusUpdates": ${type.canPublishStatusUpdates},
   "canUnpublish": ${type.canUnpublish},
   <#if type.icon??>"icon": "${type.icon}",</#if>
   "maxStatusLength": ${type.maxStatusLength}
}
</#macro>

<#-- Renders a List of Strings. -->
<#macro iterateStringsJSON strings>
<#if strings??>
[
   <#list strings as string>
   "${string}"
   <#if string_has_next>,</#if>
   </#list>
]
<#else>
[]
</#if>
</#macro>

<#-- Renders a date. -->
<#macro dateJSON date>
{
   "dateTime": "${date.dateTime}",
   "format": "${date.format}"
}
</#macro>

<#-- Renders a calendar. -->
<#macro calendarJSON calendar>
{
   "dateTime": "${calendar.dateTime}",
   "format": "${calendar.format}",
   "timeZone": "${calendar.timeZone}"
}
</#macro>

<#-- Renders the info needed about a channel's authorisation -->
<#macro channelAuthJSON>
<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "channelId" : "${channelId}",
      "authoriseUrl": "${authoriseUrl}",
      "authCallbackUrl": "${authCallbackUrl}",
      "authRedirectUrl": "${authRedirectUrl}"
   }
</#escape>
</#macro>
   