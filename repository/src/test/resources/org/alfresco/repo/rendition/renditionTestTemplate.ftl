<h6>Test Template 1</h6>

<#-- Title is used in test code to ensure that updates to source node rerender properly -->
<#-- The xxx xxx demarcation here is used by the test code -->
TestTitle= xxx${node.properties["cm:title"]?string}xxx

<#-- Test basic properties -->
${node.id}<br>
${node.name}<br>
${node.properties?size}<br>
${node.children?size}<br>

<#if node.assocs["cm:translations"]?exists>
node.assocs<br>
</#if>
${node.aspects?size}<br>
<#if node.isContainer>node.isContainer</#if><br>
<#if node.isDocument>node.isDocument</#if><br>
<#--${node.content}<br>-->
${node.url}<br>
${node.displayPath}<br>
${node.icon16}<br>
${node.icon32}<br>
<#if node.mimetype?exists>node.mimetype</#if><br>
<#if node.size?exists>node.size</#if><br>
<#if node.isLocked>node.isLocked</#if><br>

<#-- Test child walking and property resolving -->
<table>
<#list node.children as child>
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
<#list node.childrenByXPath["//*[@sys:store-protocol='workspace']"] as child>
   ${child.name}
</#list>

<h6>End Test Template 1</h6>
