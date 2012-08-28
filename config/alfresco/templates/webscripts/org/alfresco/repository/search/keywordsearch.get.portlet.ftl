    <table>
      <tr>
        <td>Results <b>${search.startIndex}</b> - <b>${search.startIndex + search.totalPageItems - 1}</b> of <b>${search.totalResults}</b> for <b>${search.searchTerms?html}</b> visible to user <b><#if person??>${person.properties.userName}<#else>unknown</#if>.</b></td>
     </tr>
    </table>
    <br>
    <table>
<#list search.results as row>            
      <tr>
      <td><img src="${url.context}${row.icon16}"/></td><td><a href="${url.serviceContext}${row.url}">${row.name?html}</a></td>
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