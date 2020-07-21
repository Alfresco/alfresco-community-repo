<#-- Table of docs in a specific folder, that have been created or modified in the last week -->
<h3>${message("templates.recent_docs.documents_created_or_modified_in_the_last_week")}</h3>
<table cellpadding=2>
   <tr>
      <td></td>
      <td><b>${message("templates.recent_docs.name")}</b></td>
      <td><b>${message("templates.recent_docs.created_date")}</b></td>
      <td><b>${message("templates.recent_docs.modified_date")}</b></td>
   </tr>
   <#list space.childrenByXPath[".//*[subtypeOf('cm:content')]"] as child>
      <#if (dateCompare(child.properties["cm:modified"], date, 1000*60*60*24*7) == 1) || (dateCompare(child.properties["cm:created"], date, 1000*60*60*24*7) == 1)>
         <tr>
            <td><a href="${url.context}${child.url}" target="new"><img src="${url.context}${child.icon16}" border=0></a></td>
            <td><a href="${url.context}${child.url}" target="new">${child.properties.name}</a></td>
            <td>${child.properties["cm:created"]?datetime}</td>
            <td>${child.properties["cm:modified"]?datetime}</td>
         </tr>
      </#if>
   </#list>
</table>