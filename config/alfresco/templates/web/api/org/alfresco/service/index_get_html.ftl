<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco Web APIs</title> 
    <link rel="stylesheet" href="/alfresco/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
     <tr>
        <td><img src="/alfresco/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Web APIs</span></nobr></td>
     </tr>
     <tr><td><td>Alfresco ${server.edition} v${server.version}
     <tr><td><td>${services?size} services - <a href="http://wiki.alfresco.com/wiki/REST">Online documentation</a>.
    </table>
	<br>
    <table border="0" cellpadding="4">
      <tr align="left"><td><b>Id<td><b>Method<td><b>Authentication</tr>
<#list services as service>
      <#assign desc = service.description>
      <tr><td>${desc.id}<td>${desc.method}<td>${desc.requiredAuthentication}</tr>
      <tr><td colspan="4">&nbsp;&nbsp;[${desc.description}]</tr>
      <tr><td colspan="4"></tr>
      <#list desc.URIs as uri>
         <tr><td>&nbsp;&nbsp;${uri.format}<#if uri.format = desc.defaultFormat> (default)</#if><td colspan="3"><a href="${path.serviceContext}${uri.URI}">${path.serviceContext}${uri.URI}</a></tr>
      </#list>
</#list>
    </table>
</html>