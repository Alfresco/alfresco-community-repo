<#-- Displays a table of all the documents from a "Press Releases" folder under Company Home -->
<#-- Obviously this folder needs to exist and the docs in it should have the title and description fields set -->
<table>
   <#list companyhome.children as child>
      <#if child.isContainer && child.name = "Press Releases">
         <#list child.children as doc>
            <#if doc.isDocument>
               <tr>
                   <td><a class="title" href="${url.context}/${doc.url}">${doc.properties.title}</a></td>
               </tr>
               <tr>
                   <td style="padding-left:4px"><b>${doc.properties.description}</b></td>
               </tr>
               <tr>
                   <td style="padding-left:8px">
                     <#if (doc.content?length > 500)>
                        <small>${doc.content[0..500]}...</small>
                     <#else>
                        <small>${doc.content}</small>
                     </#if>
                   </td>
               </tr>
               <tr><td><div style="padding:6px"></div></td></tr>
            </#if>
         </#list>
      </#if>
   </#list>
</table>