<#-- 3 column list of image "thumbnails" in the current space -->
<#assign colcount=0>
<table width=100%>
<#list space.children as child>
  <#if child.isDocument && (child.mimetype = "image/png" || child.mimetype = "image/jpeg" || child.mimetype = "image/gif")>
    <#if colcount % 3 = 0><tr></#if>
    <td align=center><div style='padding:8px'><a href="${url.context}${child.url}" target="new"><img border=0 src="${url.context}${child.url}" width=120></a></div><div>${child.properties.name}</div></td>
    <#if colcount % 3 = 2></tr></#if>
    <#assign colcount=colcount+1>
  </#if>
</#list>
</table>