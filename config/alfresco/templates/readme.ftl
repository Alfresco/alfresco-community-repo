<#-- Displays the contents of readme.html and/or the evaluated readme.ftl if they exist in the current space. -->
<#-- Old template 
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
-->
<#assign filename = "readme"/>
<#assign htmlExt = "html"/>
<#assign suffix = .lang />
<#assign ftlExt = "ftl"/>
<#assign htmlFilename = "${filename}_${suffix}.${htmlExt}"/>
<#assign ftlFilename = "${filename}.${ftlExt}"/>
<#assign messageError="File ${htmlFilename} does not exist!"/>

<#if space?exists>
    <#if space.childByNamePath["${htmlFilename}"]?exists>
        ${space.childByNamePath[htmlFilename].content}
        <#else>
        ${messageError}
    </#if>
    <#if space.childByNamePath["${ftlFilename}"]?exists>
        <#include space.childByNamePath["${ftlFilename}"].nodeRef> 
    </#if>
</#if>
