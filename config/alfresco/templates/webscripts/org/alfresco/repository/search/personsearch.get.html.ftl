<html>
  <head profile="http://a9.com/-/spec/opensearch/1.1/"> 
    <title>Alfresco Person Search: ${search.searchTerms}</title>
    <link rel="stylesheet" href="/alfresco/css/main.css" TYPE="text/css">
    <link rel="search" type="application/opensearchdescription+xml" href="${url.serviceContext}/api/search/person/description.xml" title="Alfresco Person Search"/>
    <meta name="totalResults" content="${search.totalResults}"/>
    <meta name="startIndex" content="${search.startIndex}"/>
    <meta name="itemsPerPage" content="${search.itemsPerPage}"/>
  </head>
  <body>
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Person Search</span></nobr></td>
     </tr>
    </table>
    <br>
    <table>
      <tr>
        <td>Results <b>${search.startIndex}</b> - <b><#if search.startIndex == 0>0<#else>${search.startIndex + search.totalPageItems - 1}</#if></b> of <b>${search.totalResults}</b> for <b>${search.searchTerms}</b> visible to user <b><#if person??>${person.properties.userName}<#else>unknown</#if>.</b></td>
     </tr>
    </table>
    <br>
    <table>
<#list search.results as row>            
      <tr>
      <td><img src="${url.context}${row.assocs[\"cm:avatar\"][0].url}"/></td><td><a href="${absurl(url.context)}/c/ui/userprofile?id=${row.id}">${row.firstName} ${row.lastName}</a></td>
      </tr>
      <tr>
      <td></td>
      <td>
         <#if row.properties.jobtitle??>${row.properties.jobtitle},</#if>
         <#if row.properties.organization??>${row.properties.organization},</#if>
         <#if row.properties.location??>${row.properties.location},</#if>
         <#if row.properties.persondescription??>${row.properties.persondescription.content}</#if>
      </td>
      </tr>
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
