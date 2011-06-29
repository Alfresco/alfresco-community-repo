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
   "name": "${channel.name}",
   "title": "${channel.title}",
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
   "supportedContentTypes":
   <@iterateStringsJSON strings=type.supportedContentTypes />,
   "supportedMimeTypes":
   <@iterateStringsJSON strings=type.supportedMimeTypes/>,
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
