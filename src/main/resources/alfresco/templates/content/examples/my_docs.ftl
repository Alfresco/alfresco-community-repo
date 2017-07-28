<#-- Table of the documents in my Home Space -->
<#-- Shows the Icon and link to the content for the doc, also the size in KB and lock status -->
<table>
   <tr>
      <td></td>
      <td><b>${message("templates.my_docs.name")}</b></td>
      <td><b>${message("templates.my_docs.size")}</b></td>
      <td><b>${message("templates.my_docs.locked")}</b></td>
   </tr>
   <#list userhome.children as child>
      <#if child.isDocument>
         <tr>
            <td><a href="${url.context}${child.url}" target="new"><img src="${url.context}${child.icon16}" border=0></a></td>
            <td><a href="${url.context}${child.url}" target="new">${child.properties.name}</a></td>
            <td>${(child.size / 1000)?string("0.##")} ${message("templates.my_docs.kb")}</td>
            <td>&nbsp;<#if child.isLocked>${message("templates.my_docs.yes")}</#if></td>
         </tr>
      </#if>
   </#list>
</table>