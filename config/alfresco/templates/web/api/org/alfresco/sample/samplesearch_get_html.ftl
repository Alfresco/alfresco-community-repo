<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco Keyword Search: ${args.q}</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Keyword Search: ${args.q}</span></nobr></td>
     </tr>
    </table>
    <br>
    <table>
<#list resultset as node>            
      <tr>
      <td><img src="${url.context}${node.icon16}"/></td><td><a href="${url.context}${node.url}">${node.name}</a></td>
      </tr>
      <#if node.properties.description?? == true>
      <tr>
      <td></td>
      <td>${node.properties.description}</td>
      </tr>
      </#if>
</#list>
    </table>
  </body>
</html>
