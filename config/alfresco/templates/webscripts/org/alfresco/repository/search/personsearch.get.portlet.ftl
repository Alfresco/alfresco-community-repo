    <table>
      <tr>
        <td>Results <b>${search.startIndex}</b> - <b>${search.startIndex + search.totalPageItems - 1}</b> of <b>${search.totalResults}</b> for <b>${search.searchTerms}</b> visible to user <b><#if person??>${person.properties.userName}<#else>unknown</#if>.</b></td>
     </tr>
    </table>
    <br>
    <table>
<#list search.results as row>  

      <#if (row.assocs["cm:avatar"]?exists)>
      		<#assign avatarURL = row.assocs["cm:avatar"][0].url>
   	  <#else>
      		<#assign avatarURL = "/images/icons/default_avatar.png">
   	  </#if>
      <tr>
      <td><img src="${absurl(url.context)}${avatarURL}"/></td>
      <td><a href="${absurl(url.context)}/c/ui/userprofile?id=${row.id}"><#if row.properties.firstName??>${row.properties.firstName}</#if><#if row.properties.lastName??> ${row.properties.lastName} </#if></a></td>
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