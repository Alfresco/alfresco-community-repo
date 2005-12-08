<h6>Test Template 1</h6>

<#-- Test basic properties -->
${root.id}<br>
${root.name}<br>
${root.properties?size}<br>
${root.children?size}<br>
<#if root.assocs["cm:translations"]?exists>
root.assocs<br>
</#if>
${root.aspects?size}<br>
<#if root.isContainer>root.isContainer</#if><br>
<#if root.isDocument>root.isDocumentr</#if><br>
<#--${root.content}<br>-->
${root.url}<br>
${root.displayPath}<br>
${root.icon16}<br>
${root.icon32}<br>
<#if root.mimetype?exists>root.mimetype</#if><br>
<#if root.size?exists>root.size</#if><br>
<#if root.isLocked>root.isLocked</#if><br>

<#-- Test child walking and property resolving -->
<table>
<#list root.children as child>
   <#-- show properties of each child -->
   <#assign props = child.properties?keys>
   <#list props as t>
      <#-- If the property exists -->
      <#if child.properties[t]?exists>
          <#-- If it is a date, format it accordingly-->
          <#if child.properties[t]?is_date>
          	<tr><td>${t} = ${child.properties[t]?date}</td></tr>
          
          <#-- If it is a boolean, format it accordingly-->
          <#elseif child.properties[t]?is_boolean>
          	<tr><td>${t} = ${child.properties[t]?string("yes", "no")}</td></tr>
          
          <#-- Otherwise treat it as a string -->
          <#else>
          	<tr><td>${t} = ${child.properties[t]}</td></tr>
          </#if>
      </#if>
   </#list>
   
</#list>
</table>

<#-- Test XPath -->
<#list root.childrenByXPath["//*[@sys:store-protocol='workspace']"] as child>
   ${child.name}
</#list>

<h6>End Test Template 1</h6>
