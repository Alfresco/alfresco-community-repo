<#-- Table of the documents in my Home Space -->
<#-- Shows the Icon and link to the content for the doc, also the size in KB and lock status -->
<table>
   <tr>
      <td></td>
      <td><b>Name</b></td>
      <td><b>Size</b></td>
      <td><b>Locked</b></td>
   </tr>
   <#list userhome.children as child>
      <#if child.isDocument>
         <tr>
            <td><a href="/alfresco${child.url}" target="new"><img src="/alfresco${child.icon16}" border=0></a></td>
            <td><a href="/alfresco${child.url}" target="new">${child.properties.name}</a></td>
            <td>${(child.size / 1000)?string("0.##")} KB</td>
            <td>&nbsp;<#if child.isLocked>Yes</#if></td>
         </tr>
      </#if>
   </#list>
</table>
