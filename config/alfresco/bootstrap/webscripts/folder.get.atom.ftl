<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Folder: ${folder.displayPath}/${folder.name?xml}</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<#list folder.children as child>
  <entry>
    <title>${child.name?xml}</title>
    <#if child.isContainer>
      <link rel="alternate" href="${absurl(url.serviceContext)}/sample/folder<@encodepath node=child/>"/>
    <#else>
      <link rel="alternate" href="${absurl(url.serviceContext)}/api/node/content/${child.nodeRef.storeRef.protocol}/${child.nodeRef.storeRef.identifier}/${child.nodeRef.id}/${child.name?url}"/>
    </#if>
    <icon>${absurl(url.context)}${child.icon16}</icon>
    <id>urn:uuid:${child.id}</id>
    <updated>${xmldate(child.properties.modified)}</updated>
    <summary>${child.properties.description?xml!""}</summary>
    <author> 
      <name>${child.properties.creator?xml}</name>
    </author>
  </entry>
</#list>
</feed>
<#macro encodepath node><#if node.parent?exists><@encodepath node=node.parent/>/${node.name?url}</#if></#macro>