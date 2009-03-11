<#-- Table of docs in a specific folder, that have been created or modified in the last week -->
<h3>Documents created or modified in the last week</h3>
<table cellpadding=2>
   <tr>
      <td></td>
      <td><b>Name</b></td>
      <td><b>Created Date</b></td>
      <td><b>Modified Date</b></td>
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