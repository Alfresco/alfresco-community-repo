<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Installed Web Script: ${installedScript.id}</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css"/>
  </head>
  <body>
    <table>
      <tr><td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco"/><td><nobr><span class="mainTitle">Installed Web Script: ${installedScript.id}</span></nobr>
      <tr><td><td>Alfresco ${server.edition} v${server.version}
      <tr><td><td>${date?datetime}
    </table>
	<p/>
	<table>
      <tr><td><a href="${url.serviceContext}/script/${installedScript.id}">Inspect ${installedScript.id}</a>
      <tr><td><a href="${url.serviceContext}/">Back to Web Scripts Home</a>
    </table>
    <p/>
    <table>
     <span class="mainSubTitle">Script Properties</span>
     <tr><td>Id:</td><td>${installedScript.id}</td></tr>
     <tr><td>Short Name:</td><td>${installedScript.shortName}</td></tr>
     <tr><td>Description:</td><td>${installedScript.description}</td></tr>
     <tr><td>Authentication:</td><td>${installedScript.requiredAuthentication}</td></tr>
     <tr><td>Transaction:</td><td>${installedScript.requiredTransaction}</td></tr>
     <tr><td>Method:</td><td>${installedScript.method}</td></tr>
     <#list installedScript.URIs as URI>
     <tr><td>URL Template:</td><td>${URI}</td></tr>
     </#list>
     <tr><td>Format Style:</td><td>${installedScript.formatStyle}</td></tr>
     <tr><td>Default Format:</td><td>${installedScript.defaultFormat}</td></tr>
    </table>
    <p/>
    <table>
     <span class="mainSubTitle">Files Installed</span>
     <#list installedFiles as file>
      <tr><td>${file.path}<td>(store: ${file.store})
     </#list>
    </table>
  </body>
</html>