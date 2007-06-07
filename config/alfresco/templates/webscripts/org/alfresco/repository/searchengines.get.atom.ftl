<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Registered Search Engines</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
  <author> 
    <name><#if person??>${person.properties.userName}<#else>unknown</#if></name>
  </author> 
  <id>${absurl(url.full)?xml}</id>
<#list engines as engine>
  <entry>
    <title>${engine.label}</title>
    <link rel="alternate" type="${engine.type}" href="${absurl(url.serviceContext)}${engine.url?xml}" title="${engine.label}"/>
    <summary><#if engine.urlType == "description">OpenSearch Description<#else>Template URL</#if> - ${engine.type}</summary>
    <id>${absurl(url.serviceContext)}${engine.url?xml}</id>
    <updated>${xmldate(date)}</updated>
  </entry>
</#list>
</feed>