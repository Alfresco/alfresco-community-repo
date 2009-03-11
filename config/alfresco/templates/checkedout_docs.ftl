<#-- Table of the documents checked out to the current user -->
<#-- Shows the Icon and link to the content for the doc, also the size in KB and modified date -->
<#assign query="@cm\\:lockOwner:${person.properties.userName}">
<#assign rowcount=0>
<table>
   <tr style='background-color: #C6D8EB'>
      <td></td>
      <td><b>Name</b></td>
      <td><b>Size</b></td>
      <td><b>Modified Date</b></td>
      <td><b>Location</b></td>
   </tr>
   <#list userhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
         <#if rowcount % 2 = 0><tr><#else><tr style='background-color: #DEE5EC'></#if>
            <td><a href="${url.context}${child.url}" target="new"><img src="${url.context}${child.icon16}" border=0></a></td>
            <td><a href="${url.context}${child.url}" target="new">${child.properties.name}</a></td>
            <td>${(child.size / 1000)?string("0.##")} KB</td>
            <td>${child.properties.modified?datetime}</td>
            <td>${child.displayPath}</td>
         </tr>
         <#assign rowcount=rowcount+1>
      </#if>
   </#list>
</table>