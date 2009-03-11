<#-- Shows version history for the current document, with links to previous content versions -->
<#if document?exists>
   <h3>Document Version History for: ${document.name}</h3>
   <table cellspacing=4>
      <tr align=left><th>Version</th><th>Name</th><th>Description</th><th>Created Date</th><th>Creator</th></tr>
      <#list document.versionHistory as record>
         <tr>
            <td><a href="${url.context}${record.url}" target="new">${record.versionLabel}</a></td>
            <td><a href="${url.context}${record.url}" target="new">${record.name}</a></td>
            <td><#if record.description?exists>${record.description}</#if></td>
            <td>${record.createdDate?datetime}</td>
            <td>${record.creator}</td>
         </tr>
      </#list>
   </table>
<#else>
   No document found!
</#if>