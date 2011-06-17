<#-- Renders a Channel. -->
<#macro channelJSON channel>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "url": "${channel.url}",
         "name": "${channel.name}",
         "channelType":
         <@channelTypeJSON type=channel.channelType />
      }
</#escape>
</#macro>

<#-- Renders a Channel Type. -->
<#macro channelTypeJSON type>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "id": "${type.id}",
         "url": "${type.url}",
         "channelNodeType": "${type.channelNodeType}",
         "contentRootNodeType": "${type.contentRootNodeType}",
         "supportedContentTypes":
         <@iterateStringsJSON strings=type.supportedContentTypes />,
         "supportedMimeTypes":
         <@iterateStringsJSON strings=type.supportedMimeTypes/>,
         "canPublish": "${type.canPublish}",
         "canPublishStatusUpdates": "${type.canPublishStatusUpdates}",
         "canUnpublish": "${type.canUnpublish}"
      }
</#escape>
</#macro>

<#-- Renders a List of Strings. -->
<#macro iterateStringsJSON strings>
<#escape x as jsonUtils.encodeJSONString(x)>
      [
         <#list strings as string>
         "${string}"
         <#if string_has_next>,</#if>
         </#list>
      ]
</#escape>
</#macro>
