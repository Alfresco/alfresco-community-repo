<#-- Shows use of the childByNamePath and childrenByXPath API -->

<h3>Template Documents in 'Company Home/Data Dictionary/Content Templates':</h3>
<table>
<#list companyhome.childByNamePath["Data Dictionary/Content Templates"].children as child>
   <#if child.isDocument>
      <tr><td><a href="${url.context}${child.url}" target="new">${child.properties.name}</a></td></tr>
   </#if>
</#list>
</table>

<h3>Folders in 'Company Home/Data Dictionary/Space Templates/Software Engineering Project':</h3>
<table>
<#list companyhome.childrenByXPath["*[@cm:name='Data Dictionary']/*[@cm:name='Space Templates']/*[@cm:name='Software Engineering Project']/*"] as child>
   <#if child.isContainer>
      <tr><td><img src="${url.context}${child.icon32}"> ${child.properties.name}</td></tr>
   </#if>
</#list>
</table>