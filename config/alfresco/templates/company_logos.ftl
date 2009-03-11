<#-- Table of the images found in a folder under Company Home called "Company Logos" -->
<#-- Shows each image found as inline content -->
<table>
   <#list companyhome.children as child>
      <#if child.isContainer && child.name = "Company Logos">
         <#list child.children as image>
            <#switch image.mimetype>
              <#case "image/jpeg">
              <#case "image/gif">
              <#case "image/png">
                  <tr>
                     <td><img src="/alfresco${image.url}"></td>
                  </tr>
                  <#break>
               <#default>
            </#switch>
         </#list>
      </#if>
   </#list>
</table>