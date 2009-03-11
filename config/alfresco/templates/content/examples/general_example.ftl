<h3>=====Example Template Start=====</h3>

<b>Company Home Space:</b> ${companyhome.properties.name}
<br>
<b>My Home Space:</b> ${userhome.properties.name}
<br>
<b>Company Home children count:</b> ${companyhome.children?size}
<br>
<b>Company Home first child node name:</b> ${companyhome.children[0].properties.name}
<br>
<#if document?exists>
<b>Current Document Name:</b> ${document.name}
<br>
</#if>
<b>Current Space Name:</b> ${space.name}

<h4>List of child spaces in my Home Space:</h4>
<table>
<#list userhome.children as child>
   <#if child.isContainer>
      <tr>
         <td><img src="${url.context}${child.icon32}"></td>
         <td><b>${child.properties.name}</b> (${child.children?size})</td>
         <td><b>Path:</b> ${child.displayPath}</td>
      </tr>
   </#if>
</#list>
</table>

<h4>List of docs in my Home Space (text only content shown inline, JPG images shown as thumbnails):</h4>
<table>
<#list userhome.children as child>
   <#if child.isDocument>
      <tr><td><img src="${url.context}${child.icon16}"></td><td><a href="${url.context}${child.url}">${child.properties.name}</a></td></tr>
      <#if child.mimetype = "text/plain">
         <tr><td></td><td>${child.content}</td></tr>
      <#elseif child.mimetype = "image/jpeg">
         <tr><td></td><td><img width=100 height=65 src="${url.context}${child.url}"></td></tr>
      </#if>
   </#if>
</#list>
</table>

<h4>Assoc example:</h4>
<#if userhome.children[0].assocs["cm:contains"]?exists>
   ${userhome.children[0].assocs["cm:contains"][0].name}
</#if>

<h3>=====Example Template End=====</h3>