<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head profile="http://a9.com/-/spec/opensearch/1.1/"> 
    <title>Alfresco Keyword Search: ${search.searchTerms?html}</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
    <link rel="search" type="application/opensearchdescription+xml" href="${url.serviceContext}/api/search/keyword/description.xml" title="Alfresco Keyword Search"/>
    <meta name="totalResults" content="${search.totalResults}"/>
    <meta name="startIndex" content="${search.startIndex}"/>
    <meta name="itemsPerPage" content="${search.itemsPerPage}"/>
  </head>
  <body>
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Keyword Search</span></nobr></td>
     </tr>
    </table>
    <br>
    <table>
      <tr>
        <td>Results <b>${search.startIndex}</b> - <b><#if search.startIndex == 0>0<#else>${search.startIndex + search.totalPageItems - 1}</#if></b> of <b>${search.totalResults}</b> for <b>${search.searchTerms?html}</b> visible to user <b><#if person??>${person.properties.userName}<#else>unknown</#if>.</b></td>
     </tr>
    </table>
    <br>
    <table>
<#list search.results as row>            
      <tr>
      <td><img src="${url.context}${row.icon16}"/></td><td><a href="${url.serviceContext}${row.url}">${row.name}</a></td>
      </tr>
      <#if row.properties.description?? == true>
      <tr>
      <td></td>
      <td>${row.properties.description?html}</td>
      </tr>
      </#if>
</#list>
    </table>
    <br>
    <table>
      <tr>
        <td><a href="${scripturl("?q=${search.searchTerms?url}&p=1&c=${search.itemsPerPage}&l=${search.localeId}")}">first</a></td>
<#if search.startPage &gt; 1>
        <td><a href="${scripturl("?q=${search.searchTerms?url}&p=${search.startPage - 1}&c=${search.itemsPerPage}&l=${search.localeId}")}">previous</a></td>
</#if>
        <td><a href="${scripturl("?q=${search.searchTerms?url}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}")}">${search.startPage}</a></td>
<#if search.startPage &lt; search.totalPages>
        <td><a href="${scripturl("?q=${search.searchTerms?url}&p=${search.startPage + 1}&c=${search.itemsPerPage}&l=${search.localeId}")}">next</a></td>
</#if>
        <td><a href="${scripturl("?q=${search.searchTerms?url}&p=${search.totalPages}&c=${search.itemsPerPage}&l=${search.localeId}")}">last</a></td>
      </tr>
    </table>
  </body>
</html>
