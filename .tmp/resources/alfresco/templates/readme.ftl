<#-- Displays the contents of readme.html and/or the evaluated readme.ftl if they exist in the current space. -->
<#assign filename = "readme"/>
<#assign htmlExt = "html"/>
<#assign suffix = .lang />
<#assign ftlExt = "ftl"/>
<#assign htmlLangFilename = "${filename}_${suffix}.${htmlExt}"/>
<#assign htmlFilename = "${filename}.${htmlExt}"/>
<#assign ftlFilename = "${filename}.${ftlExt}"/>
<#assign messageError="Readme file does not exist!"/>
<#assign found = false>

<#if space?exists>
    <#if space.childByNamePath["${htmlLangFilename}"]?exists>
        ${space.childByNamePath[htmlLangFilename].content}
    <#elseif space.childByNamePath["${htmlFilename}"]?exists>
        ${space.childByNamePath[htmlFilename].content}
    <#elseif space.childByNamePath["${ftlFilename}"]?exists>
        <#include space.childByNamePath["${ftlFilename}"].nodeRef> 
    <#else>
        ${messageError}
    </#if>
</#if>