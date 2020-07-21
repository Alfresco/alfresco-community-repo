<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:relevance="http://a9.com/-/opensearch/extensions/relevance/1.0/" xmlns:alf="http://www.alfresco.org/opensearch/1.0/">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Alfresco Person Search: ${search.searchTerms?xml}</title>
  <updated>${xmldate(date)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
  <author> 
    <name><#if person??>${person.properties.userName}<#else>unknown</#if></name>
  </author> 
  <id>urn:uuid:${search.id}</id>
  <opensearch:totalResults>${search.totalResults}</opensearch:totalResults>
  <opensearch:startIndex>${search.startIndex}</opensearch:startIndex>
  <opensearch:itemsPerPage>${search.itemsPerPage}</opensearch:itemsPerPage>
  <opensearch:Query role="request" searchTerms="${search.searchTerms?xml}" startPage="${search.startPage}" count="${search.itemsPerPage}" language="${search.localeId}"/>
  <link rel="alternate" href="${absurl(scripturl("?q=${search.searchTerms?url?xml}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="text/html"/>
  <link rel="self" href="${absurl(scripturl("?q=${search.searchTerms?url?xml}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
<#if search.startPage &gt; 1>
  <link rel="first" href="${absurl(scripturl("?q=${search.searchTerms?url?xml}&p=1&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
  <link rel="previous" href="${absurl(scripturl("?q=${search.searchTerms?url?xml}&p=${search.startPage - 1}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
</#if>
<#if search.startPage &lt; search.totalPages>
  <link rel="next" href="${absurl(scripturl("?q=${search.searchTerms?url?xml}&p=${search.startPage + 1}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
  <link rel="last" href="${absurl(scripturl("?q=${search.searchTerms?url?xml}&p=${search.totalPages}&c=${search.itemsPerPage}&l=${search.localeId}")?xml)}" type="application/atom+xml"/>
</#if>
  <link rel="search" type="application/opensearchdescription+xml" href="${absurl(url.serviceContext)}/api/search/person/description.xml"/>
<#list search.results as row>
  <entry>
    <title><#if row.properties.firstName??>${row.properties.firstName?xml}</#if> <#if row.properties.lastName??>${row.properties.lastName?xml}</#if></title>
    <link rel="alternate" href="${absurl(url.context)}/c/ui/userprofile?id=${row.id}"/>
<#if row.assocs["cm:avatar"]?exists>
    <icon>${absurl(url.context)}${row.assocs["cm:avatar"][0].url}</icon>
<#else>
    <icon>${absurl(url.context)}/images/icons/user_large.gif</icon>
</#if>
    <id>urn:uuid:${row.id}</id>
    <updated>${xmldate(row.properties.modified)}</updated>
    <summary>
       <#if row.properties.jobtitle??>${row.properties.jobtitle?xml},</#if>
       <#if row.properties.organization??>${row.properties.organization?xml},</#if>
       <#if row.properties.location??>${row.properties.location?xml},</#if>
       <#if row.properties.persondescription??>${row.properties.persondescription.content?xml}</#if>
    </summary>
    <relevance:score>${row.score}</relevance:score>
  </entry>
</#list>
</feed>