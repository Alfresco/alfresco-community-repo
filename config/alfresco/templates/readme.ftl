<#-- Displays the contents of readme.html and/or the evaluated readme.ftl if they exist in the current space. -->
 
<#assign htmlFilename = "readme.html"/>
<#assign ftlFilename = "readme.ftl"/>
 
<#if space?exists>
    <#if space.childByNamePath["${htmlFilename}"]?exists>
        ${space.childByNamePath[htmlFilename].content}
    </#if>
    <#if space.childByNamePath["${ftlFilename}"]?exists>
        <#include space.childByNamePath["${ftlFilename}"].nodeRef> 
    </#if>
</#if>
