<h3>${message("templates.general_example.example_template_start")}</h3>

<b>${message("templates.general_example.company_home_space")}</b> ${companyhome.properties.name}
<br>
<b>${message("templates.general_example.my_home_space")}</b> ${userhome.properties.name}
<br>
<b>${message("templates.general_example.company_home_children_count")}</b> ${companyhome.children?size}
<br>
<b>${message("templates.general_example.company_home_first_child_node_name")}</b> ${companyhome.children[0].properties.name}
<br>
<#if document?exists>
<b>${message("templates.general_example.current_document_name")}</b> ${document.name}
<br>
</#if>
<b>${message("templates.general_example.current_space_name")}</b> ${space.name}

<h4>${message("templates.general_example.list_of_child_spaces_in_my_home_space")}</h4>
<table>
<#list userhome.children as child>
   <#if child.isContainer>
      <tr>
         <td><img src="${url.context}${child.icon32}"></td>
         <td><b>${child.properties.name}</b> (${child.children?size})</td>
         <td><b>${message("templates.general_example.path")}</b> ${child.displayPath}</td>
      </tr>
   </#if>
</#list>
</table>

<h4>${message("templates.general_example.list_of_docs_in_my_home_space")}</h4>
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

<h4>${message("templates.general_example.assoc_example")}</h4>
<#if userhome.children[0].assocs["cm:contains"]?exists>
   ${userhome.children[0].assocs["cm:contains"][0].name}
</#if>

<h3>${message("templates.general_example.example_template_end")}</h3>