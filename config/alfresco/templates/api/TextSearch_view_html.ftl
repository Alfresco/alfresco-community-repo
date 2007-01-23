<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head profile="http://a9.com/-/spec/opensearch/1.1/"> 
    <title>Alfresco Text Search: ${search.searchTerms}</title> 
    <link rel="search" type="application/opensearchdescription+xml" href="${request.servicePath}/search/text/textsearchdescription.xml" title="Alfresco Text Search"/>
    <meta name="totalResults" content="${search.totalResults}"/>
    <meta name="startIndex" content="${search.startIndex}"/>
    <meta name="itemsPerPage" content="${search.itemsPerPage}"/>
  </head>
  <body>
    <h2>Alfresco Text Search</h2>
    Results <b>${search.startIndex}</b> - <b>${search.startIndex + search.totalPageItems - 1}</b> of <b>${search.totalResults}</b> for <b>${search.searchTerms}</b> visible to user <b>${request.authenticatedUsername!"unknown"}.</b>
    <ul>
<#list search.results as row>            
      <li>
        <img src="${absurl(row.icon16)}"/>
        <a href="${absurl(row.url)}">${row.name}</a>
        <div>
          ${row.properties.description!""}
        </div>
      </li>
</#list>
    </ul>
    <a href="${request.servicePath}/search/text?q=${search.searchTerms?url}&p=1&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string("true","")}">first</a>
<#if search.startPage &gt; 1>
    <a href="${request.servicePath}/search/text?q=${search.searchTerms?url}&p=${search.startPage - 1}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string("true","")}">previous</a>
</#if>
    <a href="${request.servicePath}/search/text?q=${search.searchTerms?url}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string("true","")}">${search.startPage}</a>
<#if search.startPage &lt; search.totalPages>
    <a href="${request.servicePath}/search/text?q=${search.searchTerms?url}&p=${search.startPage + 1}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string("true","")}">next</a>
</#if>
    <a href="${request.servicePath}/search/text?q=${search.searchTerms?url}&p=${search.totalPages}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string("true","")}">last</a>
  </body>
</html>