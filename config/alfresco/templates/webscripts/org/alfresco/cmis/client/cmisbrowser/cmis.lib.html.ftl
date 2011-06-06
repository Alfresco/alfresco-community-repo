<#macro head>
<head>
  <title><#nested></title>
  <link type="text/css" rel="stylesheet" href="${url.context}/cmis-browser-app/css/style.css" />
</head>
</#macro>

<#macro header>
<div id="header">
  <img id="cmis-logo" src="${url.context}/cmis-browser-app/images/cmis-browser-logo.png" width="222" height="35" />
  <a href="http://chemistry.apache.org" target="_blank"><img id="apachec-logo"src="${url.context}/cmis-browser-app/images/apache_chemistry.png" width="169" height="21" /></a>
  <img id="divider" src="${url.context}/cmis-browser-app/images/divider.jpg" width="2" height="28"/>
  <a href="http://www.springsurf.org/" target="_blank"><img id="surf-logo" src="${url.context}/cmis-browser-app/images/surf-logo.png" width="65" height="26" /></a>
</div>
</#macro>

<#macro navigation>
<@header/>
<div id="navigation">
  <ul id="pathlist">
    <li><a class="pathlinks" href="${url.serviceContext}/cmis-browser-app/connections">CMIS Repositories</a> :</li>
    <li><#nested></li>
  </ul>
</div>
</#macro>

<#macro connectionNavigation connection>
<@header/>
<div id="navigation">
  <ul id="pathlist">
    <li><a class="pathlinks" href="${url.serviceContext}/cmis-browser-app/connections">CMIS Repositories</a> :</li>
    <li>Repository <a class="pathlinks" href="<@cmisContextUrl connection.name/>/repo">${connection.server.description!""} (${connection.userName!"<i>anonymous</i>"})</a> : </li>
    <li><#nested></li>
  </ul>
</div>
</#macro>

<#macro cmisContextUrl connectionId=""><#if connectionId != "">${url.serviceContext}/cmis-browser-app/c/${connectionId}<#-- TODO: remove all the hard-coded dirtyness --><#elseif url.match?starts_with("/cmis-browser-app/c/")>${url.serviceContext}/cmis-browser-app/c/${url.templateArgs["conn"]}<#else>${url.serviceContext}/cmis-browser-app</#if></#macro>

<#macro propertyvalue property connectionName="">
<#-- TODO: consider using single as well as list representations of value -->
<#list property.values as val>
  <#-- TODO: use property definition type instead of property id -->
  <#if property.id?? && property.id == "cmis:objectId">
    <@value val ; v><a href="<@cmisContextUrl connectionName/>/object?id=${v}">${v}</a></@value>
  <#else>
    <@value val ; v>${v}</@value>
  </#if>
  <#if val_has_next><#nested></#if>
</#list>
</#macro>

<#macro value val>
<#if val?is_hash && val.time??><#nested val.time?datetime>
<#elseif val?is_string><#nested val>
<#elseif val?is_number><#nested val?c>
<#elseif val?is_boolean><#nested val?string>
<#elseif val?is_date><#nested val?datetime>
<#else><#nested val>
</#if>
</#macro>

<#macro resultsHeader skipCount pageNumItems>
<#if pageNumItems == 0>No Results Found<#else>Results <b>${skipCount + 1}</b> - <b>${skipCount + pageNumItems}</#if></b>
</#macro>

<#macro objectImage baseTypeId="cmis:document">
<#if baseTypeId == "cmis:folder"><@image "folder-yellow"/><#else><@image "document"/></#if>
</#macro>

<#macro image image height=-1 width=-1>
<img<#if height !=-1> height="${height}"</#if><#if width !=-1> width="${width}"</#if> src="${url.context}/cmis-browser-app/images/${image}<#if image?index_of(".") == -1>.gif</#if>">
</#macro>

<#macro button name>
<input class="button" type="submit" value="${name}" onmouseover="this.className='btn-hover'" onmouseout="this.className='button'">
</#macro>