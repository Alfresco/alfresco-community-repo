<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:relevance="http://a9.com/-/opensearch/extensions/relevance/1.0/">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Alfresco Keyword Search: ${args.q}</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl("/images/logo/AlfrescoLogo16.ico")}</icon>
  <author> 
    <name><#if person??>${person.properties.userName}<#else>unknown</#if></name>
  </author> 
<#list resultset as node>
  <entry>
    <title>${node.name}</title>
    <link rel="alternate" href="${node.url}"/>
    <icon>${node.icon16}</icon>
    <id>urn:uuid:${node.id}</id>
    <updated>${xmldate(node.properties.modified)}</updated>
    <summary>${node.properties.description!""}</summary>
    <author> 
      <name>${node.properties.creator}</name>
    </author> 
  </entry>
</#list>
</feed>