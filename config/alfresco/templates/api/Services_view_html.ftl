<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head profile="http://a9.com/-/spec/opensearch/1.1/"> 
    <title>Alfresco Web APIs</title> 
    <link rel="stylesheet" href="/alfresco/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
     <tr>
        <td><img src="/alfresco/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Web APIs</span></nobr></td>
     </tr>
     <tr><td><td>Alfresco ${agent.edition} v${agent.version}
     <tr><td><td>${services?size} services - <a href="http://wiki.alfresco.com/wiki/REST">Online documentation</a>.
    </table>
	<br>
    <table border="0" cellpadding="4">
      <tr align="left"><td><b>Name<td><b>Method<td><b>URL<td><b>Authentication<td><b>Default Format</tr>
<#list services as service>            
      <tr><td>${service.name}<td>${service.httpMethod}<td><a href="${request.servicePath}${service.httpUri}">${service.httpUri}</a><td>${service.requiredAuthentication}<td>${service.defaultFormat}</tr>
      <tr><td colspan="5">&nbsp;&nbsp;[${service.description}]</tr>
      <tr><td colspan="5"></tr>
</#list>
    </table>
</html>