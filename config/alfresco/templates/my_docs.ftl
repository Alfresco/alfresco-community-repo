<#-- Table of the documents in my Home Space -->
<#-- Shows the Icon and link to the content for the doc, also the size in KB and lock status -->
<#assign rowcount=0>
<table>
   <tr style='background-color: #C6D8EB'>
      <td></td>
      <td><b>Name</b></td>
      <td><b>Size</b></td>
      <td><b>Modified Date</b></td>
      <td><b>Locked By</b></td>
   </tr>
   <#list userhome.children as child>
      <#if child.isDocument>
         <#if rowcount % 2 = 0><tr><#else><tr style='background-color: #DEE5EC'></#if>
            <td><a href="/alfresco${child.url}" target="new"><img src="/alfresco${child.icon16}" border=0></a></td>
            <td><a href="/alfresco${child.url}" target="new">${child.properties.name}</a></td>
            <td>${(child.size / 1000)?string("0.##")} KB</td>
            <td>${child.properties.modified?datetime}</td>
            <td>&nbsp;<#if child.isLocked>${child.properties.lockOwner}</#if></td>
         </tr>
         <#assign rowcount=rowcount+1>
      </#if>
   </#list>
</table>
