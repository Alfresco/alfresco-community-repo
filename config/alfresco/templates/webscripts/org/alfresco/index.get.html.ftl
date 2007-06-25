<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Web Scripts Home</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <form action="${url.serviceContext}${url.match}" method="post">
       <input type="hidden" name="reset" value="on">
       <table>
	     <tr>
	        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
	        <td><nobr><span class="mainTitle">Web Scripts Home</span></nobr></td>
	     </tr>
	     <tr><td><td>Alfresco ${server.edition} v${server.version}
	     <tr><td><td>${webscripts?size} Web Scripts - <a href="http://wiki.alfresco.com/wiki/HTTP_API">Online documentation</a>. <input type="submit" name="submit" value="Refresh list of Web Scripts">
	    </table>
    </form>     
	<br>
	<br>
    <span class="mainSubTitle">Index</span>
    <table>
        <tr><td><a href="${url.serviceContext}/index/all">Browse all Web Scripts</a>
        <tr><td><a href="${url.serviceContext}/index/uri/">Browse by Web Script URL</a>
        <tr><td><a href="${url.serviceContext}/index/package/">Browse by Web Script Package</a>
    </table>
    <br>
    <br>
    <span class="mainSubTitle">Maintenance</span>
    <table>
        <tr><td><a href="${url.serviceContext}/api/javascript/debugger">Alfresco Javascript Debugger</a>
    </table>
  </body>
</html>