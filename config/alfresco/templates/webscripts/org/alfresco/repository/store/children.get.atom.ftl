<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:cmis="http://www.cmis.org/CMIS/1.0" xmlns:alf="http://www.alfresco.org">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>${node.name}</title> 
  <updated>${xmldate(node.properties.modified)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
  <author> 
    <name>${node.properties.creator!""}</name>
  </author> 
  <id>urn:uuid:${node.id}</id>
  <link rel="self" href="${absurl(url.full)}" type="application/atom+xml"/>

<#if cursor.pageType = "WINDOW">
<#if cursor.hasFirstPage>
  <link rel="first" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.firstPage)))}" type="application/atom+xml"/>
</#if>  
<#if cursor.hasLastPage>
  <link rel="last" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.lastPage)))}" type="application/atom+xml"/>
</#if>  
<#if cursor.hasPrevPage>
  <link rel="prev" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.prevPage)))}" type="application/atom+xml"/>
</#if>  
<#if cursor.hasNextPage>
  <link rel="next" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.nextPage)))}" type="application/atom+xml"/>
</#if>  
<#else>
<#if cursor.hasFirstPage>
  <link rel="first" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.firstPage)))}" type="application/atom+xml"/>
</#if>  
<#if cursor.hasLastPage>
  <link rel="last" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.lastPage)))}" type="application/atom+xml"/>
</#if>  
<#if cursor.hasPrevPage>
  <link rel="prev" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.prevPage)))}" type="application/atom+xml"/>
</#if>  
<#if cursor.hasNextPage>
  <link rel="next" href="${absurl(scripturl(argreplace(url.args, "skipCount", cursor.nextPage)))}" type="application/atom+xml"/>
</#if>  
</#if>
  
<#list results as child>
  <entry>
    <title>${child.name}</title>
    <link rel="alternate" href="${absurl(url.serviceContext)}${child.url}"/>
    <icon>${absurl(url.context)}${child.icon16}</icon>       <#comment>TODO: What's the standard for entry icons?</#comment>
    <id>urn:uuid:${child.id}</id>
    <updated>${xmldate(child.properties.modified)}</updated>
    <published>${xmldate(child.properties.created)}</published>
    <summary>${child.properties.description!""}</summary>
    <author> 
      <name>${child.properties.creator}</name>
    </author> 
    <alf:noderef>${child.nodeRef}</alf:noderef>
  </entry>
</#list>

</feed>