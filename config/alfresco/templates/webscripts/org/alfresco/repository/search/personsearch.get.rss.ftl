<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:atom="http://www.w3.org/2005/Atom">
<channel>
  <title>Alfresco Person Search: ${search.searchTerms}</title>
  <link>${absurl(scripturl("?q=${search.searchTerms?url}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}"</link>
  <description>Alfresco Person Search: ${search.searchTerms}</description>
  <language>${search.localeId}</language>
  <lastBuildDate>${xmldate(date)}</lastBuildDate>
  <pubDate>${xmldate(date)}</pubDate>
  <generator>Alfresco ${server.edition} v${server.version}</generator>
  <image>
    <title>Alfresco Search: ${search.searchTerms}</title>
    <width>16</width>
    <height>16</height>
    <url>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico)}</url>
  </image>
  <opensearch:totalResults>${search.totalResults}</opensearch:totalResults>
  <opensearch:startIndex>${search.startIndex}</opensearch:startIndex>
  <opensearch:itemsPerPage>${search.itemsPerPage}</opensearch:itemsPerPage>
  <opensearch:Query role="request" searchTerms="${search.searchTerms}" startPage="${search.startPage}" count="${search.itemsPerPage}" language="${search.localeId}"/>
  <atom:link rel="self" href="${absurl(scripturl("?q=${search.searchTerms?url}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
<#if search.startPage &gt; 1>
  <atom:link rel="first" href="${absurl(scripturl("?q=${search.searchTerms?url}&p=1&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
  <atom:link rel="previous" href="${absurl(scripturl("?q=${search.searchTerms?url}&p=${search.startPage - 1}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
</#if>
<#if search.startPage &lt; search.totalPages>
  <atom:link rel="next" href="${absurl(scripturl("?q=${search.searchTerms?url}&p=${search.startPage + 1}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/> 
  <atom:link rel="last" href="${absurl(scripturl("?q=${search.searchTerms?url}&p=${search.totalPages}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
</#if>
  <atom:link rel="search" type="application/opensearchdescription+xml" href="${absurl(url.serviceContext)}/api/search/person/description.xml"/>
<#list search.results as row>            
  <item>
    <title>${row.firstName} ${row.lastName}</title>
    <link>${absurl(url.context)}/c/ui/userprofile?id=${row.id}</link>
    <description>
      <#if row.properties.jobtitle??>${row.properties.jobtitle},</#if>
      <#if row.properties.organization??>${row.properties.organization},</#if>
      <#if row.properties.location??>${row.properties.location},</#if>
      <#if row.properties.persondescription??>${row.properties.persondescription.content}</#if>
    </description>
    <pubDate>${xmldate(row.properties.modified)}</pubDate>
    <guid isPermaLink="false">${row.id}</guid>
  </item>
</#list>
</channel>
</rss>