<#-- Renders a List of Channel.s -->
<#macro channelsJSON channels>
<#escape x as jsonUtils.encodeJSONString(x)>
      [
         <#list channels as channel>
         <@channelJSON channel=channel />
         <#if channel_has_next>,</#if>
         </#list>
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
         "channelNodeType": "${type.channelNodeType}",
         "contentRootNodeType": "${type.contentRootNodeType}",
         "supportedContentTypes":
         <@iterateStringsJSON strings=type.supportedContentTypes />,
         "supportedMimeTypes":
         <@iterateStringsJSON strings=type.supportedMimeTypes/>,
         "canPublish": ${type.canPublish},
         "canPublishStatusUpdates": ${type.canPublishStatusUpdates},
         "canUnpublish": ${type.canUnpublish},
         "maxStatusLength": ${type.maxStatusLength},
         "icon": "${type.icon}"
      }
</#macro>

<#-- Renders a List of Strings. -->
<#macro iterateStringsJSON strings>
      [
         <#list strings as string>
         "${string}"
         <#if string_has_next>,</#if>
         </#list>
      ]
</#macro>
