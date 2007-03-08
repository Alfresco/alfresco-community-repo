<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco Web APIs Maintenance</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
     <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Alfresco Web APIs Maintenance</span></nobr></td>
     </tr>
     <tr><td><td>Alfresco ${server.edition} v${server.version}
     <tr><td><td><a href="${url.match}">${webscripts?size} apis</a>.
    </table>
	<br>
    <table border="0" cellpadding="4">
      <tr align="left"><td><b>Completed Task</tr>
<#list tasks as task>
      <tr><td>${task}<td></tr>
</#list>
    </table>
</html>