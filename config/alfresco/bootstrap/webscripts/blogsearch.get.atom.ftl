<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Blog query: ${args.q}</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<#list resultset as node>
  <entry>
    <title>${node.name}</title>
    <link rel="alternate" href="${absurl(url.serviceContext)}/api/node/content/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/${node.name?url}"/>
    <icon>${absurl(url.context)}${node.icon16}</icon>
    <id>urn:uuid:${node.id}</id>
    <updated>${xmldate(node.properties.modified)}</updated>
    <summary>${node.properties.description!""}</summary>
    <author> 
      <name>${node.properties.creator}</name>
    </author> 
  </entry>
</#list>
</feed>