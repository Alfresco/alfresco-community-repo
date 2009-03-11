<#-- List of docs in the Home Space for current user -->
<#-- If the doc mimetype is plain/text then the content is shown inline -->
<#-- If the doc mimetype is JPEG then the image is shown inline as a small thumbnail image -->
<table>
<#list userhome.children as child>
  <#if child.isDocument>
    <tr><td>${child.properties.name}</td></tr>
    <#if child.mimetype = "text/plain">
       <tr><td style='padding-left:16px'>${child.content}</td></tr>
    <#elseif child.mimetype = "image/jpeg">
       <tr><td style='padding-left:16px'><img width=100 height=65 src="${url.context}${child.url}"><td></tr>
    </#if>
  </#if>
</#list>
</table>