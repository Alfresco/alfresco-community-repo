<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco Web Scripts</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
     <form action="${url.match}" method="post">
       <input type="hidden" name="reset" value="on">
       <table>
	     <tr>
	        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
	        <td><nobr><span class="mainTitle">Alfresco Web Scripts</span></nobr></td>
	     </tr>
	     <tr><td><td>Alfresco ${server.edition} v${server.version}
	     <tr><td><td>${webscripts?size} Web Scripts - <a href="http://wiki.alfresco.com/wiki/HTTP_API">Online documentation</a>. <input type="submit" name="submit" value="Refresh list of Web Scripts">
	    </table>
    </form>     
	<br>
	<br>
    <#list webscripts as webscript>
    <#assign desc = webscript.description>
    <span class="mainSubTitle">${desc.shortName}</span>
    <table>
    <#list desc.URIs as uri>
      <tr><td><a href="${url.serviceContext}${uri.URI}">${desc.method} ${url.serviceContext}${uri.URI}</a> => ${uri.format}<#if uri.format = desc.defaultFormat> (default)</#if>
    </#list>
      <tr><td>
    </table>
    <table>
      <#if desc.description??><tr><td>Description:<td>${desc.description}<#else></#if>
      <tr><td>Authentication:<td>${desc.requiredAuthentication}
      <tr><td>Transaction:<td>${desc.requiredTransaction}
      <tr><td>
      <tr><td>Id:<td>${desc.id}
      <tr><td>Description:<td><a href="${url.serviceContext}/description/${desc.id}">${desc.storePath}/${desc.descPath}</a>
    </table>
    <br>
    </#list>
  </body>
</html>