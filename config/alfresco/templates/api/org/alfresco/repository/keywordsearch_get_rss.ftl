<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:atom="http://www.w3.org/2005/Atom">
<channel>
  <title>Alfresco Keyword Search: ${search.searchTerms}</title>
  <link>${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}</link>
  <description>Alfresco Keyword Search: ${search.searchTerms}</description>
  <language>${search.localeId}</language>
  <lastBuildDate>${xmldate(date)}</lastBuildDate>
  <pubDate>${xmldate(date)}</pubDate>
  <generator>Alfresco ${agent.edition} v${agent.version}</generator>
  <image>
    <title>Alfresco Search: ${search.searchTerms}</title>
    <width>16</width>
    <height>16</height>
    <url>${absurl("/images/logo/AlfrescoLogo16.ico")}</url>
  </image>
  <opensearch:totalResults>${search.totalResults}</opensearch:totalResults>
  <opensearch:startIndex>${search.startIndex}</opensearch:startIndex>
  <opensearch:itemsPerPage>${search.itemsPerPage}</opensearch:itemsPerPage>
  <opensearch:Query role="request" searchTerms="${search.searchTerms}" startPage="${search.startPage}" count="${search.itemsPerPage}" language="${search.localeId}"/>
  <atom:link rel="self" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
<#if search.startPage &gt; 1>
  <atom:link rel="first" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=1&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
  <atom:link rel="previous" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage - 1}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
</#if>
<#if search.startPage &lt; search.totalPages>
  <atom:link rel="next" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.startPage + 1}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/> 
  <atom:link rel="last" href="${request.servicePath}/search/keyword?q=${search.searchTerms?url}&amp;p=${search.totalPages}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string("true","")}&amp;format=${request.format}" type="application/atom+xml"/>
</#if>
  <atom:link rel="search" type="application/opensearchdescription+xml" href="${request.servicePath}/search/keywordsearchdescription.xml"/>
<#list search.results as row>            
  <item>
    <title>${row.name}</title>
    <link>${absurl(row.url)}</link>
    <description>${row.properties.description!""}</description>
    <pubDate>${xmldate(row.properties.modified)}</pubDate>
    <guid isPermaLink="false">${row.id}</guid>
  </item>
</#list>
</channel>
</rss>