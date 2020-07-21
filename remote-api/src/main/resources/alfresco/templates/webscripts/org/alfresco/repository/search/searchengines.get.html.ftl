<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head profile="http://a9.com/-/spec/opensearch/1.1/"> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
    <title>Alfresco Registered Search Engines</title>
<#list engines as engine>
<#if engine.urlType == "description">
    <link rel="search" type="${engine.type}" href="${url.serviceContext}${engine.url}" title="${engine.label?html}">
</#if>
</#list>
  </head>
  <body>
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Registered Search Engines</span></nobr></td>
     </tr>
    </table>
    <br>
    <table border="0" cellpadding="4">
      <tr align="left"><td><b>Engine<td><b>URL Type<td><b>Response Format</tr>
<#list engines as engine>
      <tr align="left">
        <td><a href="${url.serviceContext}${engine.url}">${engine.label?html}</a>
        <td><#if engine.urlType == "description">OpenSearch Description<#else>Template URL</#if>
        <td>${engine.type}
      </tr>
</#list>
    </table>
  </body>
</html>