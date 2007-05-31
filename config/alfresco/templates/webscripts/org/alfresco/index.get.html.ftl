<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Web Scripts Documentation</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <form action="${url.match}" method="post">
       <input type="hidden" name="reset" value="on">
       <table>
	     <tr>
	        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
	        <td><nobr><span class="mainTitle">Web Scripts Documentation</span></nobr></td>
	     </tr>
	     <tr><td><td>Alfresco ${server.edition} v${server.version}
	     <tr><td><td>${webscripts?size} Web Scripts - <a href="http://wiki.alfresco.com/wiki/HTTP_API">Online documentation</a>. <input type="submit" name="submit" value="Refresh list of Web Scripts">
	    </table>
    </form>     
	<br>
	<br>
    <table>
        <tr><td><a href="${url.serviceContext}/index/all">Browse all Web Scripts</a>
    </table>
    <br>
    <#macro recurseuri uri>
       <#if uri.scripts?size &gt; 0>
           <tr><td><a href="${url.serviceContext}/index/uri${uri.path}">${uri.path}</a>
       </#if>
       <#list uri.children as childpath>
          <@recurseuri uri=childpath/>
       </#list>  
    </#macro>
    <span class="mainSubTitle">Web Scripts by URL</span>
    <table>
       <@recurseuri uri=rooturl/>
    </table>
    <br>
    <#macro recursepackage package>
       <#if package.scripts?size &gt; 0>
          <tr><td><a href="${url.serviceContext}/index/package${package.path}">${package.path}</a>
       </#if>
       <#list package.children as childpath>
          <@recursepackage package=childpath/>
       </#list>  
    </#macro>
    <span class="mainSubTitle">Web Scripts by Package</span>
    <table>
       <@recursepackage package=rootpackage/>
    </table>
  </body>
</html>