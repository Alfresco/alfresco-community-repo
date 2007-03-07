<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:relevance="http://a9.com/-/opensearch/extensions/relevance/1.0/">
  <generator version="${agent.version}">Alfresco (${agent.edition})</generator>
  <title>Alfresco Keyword Search: ${search.searchTerms}</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl("/images/logo/AlfrescoLogo16.ico")}</icon>
  <author> 
    <name>${request.authenticatedUsername!"unknown"}</name>
  </author> 
  <id>urn:uuid:${search.id}</id>
  <opensearch:totalResults>${search.totalResults}</opensearch:totalResults>
  <opensearch:startIndex>${search.startIndex}</opensearch:startIndex>
  <opensearch:itemsPerPage>${search.itemsPerPage}</opensearch:itemsPerPage>
  <opensearch:Query role="request" searchTerms="${search.searchTerms}" startPage="${search.startPage}" count="${search.itemsPerPage}" language="${search.localeId}"/>
  <link rel="alternate" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=html" type="text/html"/>
  <link rel="self" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
<#if search.startPage &gt; 1>
  <link rel="first" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=1&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
  <link rel="previous" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage - 1}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
</#if>
<#if search.startPage &lt; search.totalPages>
  <link rel="next" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage + 1}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/> 
  <link rel="last" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.totalPages}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
</#if>
  <link rel="search" type="application/opensearchdescription+xml" href="${request.servicePath}/search/keywordsearchdescription.xml"/>
<#list search.results as row>
<#attempt>
  <entry>
    <title>${row.name}</title>
    <link rel="alternate" href="${absurl(row.url)}"/>
    <icon>${absurl(row.icon16)}</icon>       <#comment>TODO: What's the standard for entry icons?</#comment>
    <id>urn:uuid:${row.id}</id>
    <updated>${xmldate(row.properties.modified)}</updated>
    <summary>${row.properties.description!""}</summary>
    <author> 
      <name>${row.properties.creator}</name>
    </author> 
    <relevance:score>${row.score}</relevance:score>
  </entry>
<#recover>
</#attempt>
</#list>
</feed>