<#-- Table of the Spaces in my Home Folder -->
<#-- Shows the large 32x32 pixel icon, and generates an external access servlet URL to the space -->
<table>
   <#list userhome.children as child>
      <#if child.isContainer>
         <tr>
             <td><img src="${url.context}${child.icon32}"></td>
             <#assign ref=child.nodeRef>
             <#assign workspace=ref[0..ref?index_of("://")-1]>
             <#assign storenode=ref[ref?index_of("://")+3..]>
             <td><a href="${url.context}/navigate/showSpaceDetails/${workspace}/${storenode}"><b>${child.properties.name}</b></a> (${child.children?size})</td>
         </tr>
      </#if>
   </#list>
</table>