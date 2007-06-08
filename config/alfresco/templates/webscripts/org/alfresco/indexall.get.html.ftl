<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Index of All Web Scripts</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
   <table>
     <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Index of All Web Scripts</span></nobr></td>
     </tr>
     <tr><td><td>Alfresco ${server.edition} v${server.version}
     <tr><td><td>${webscripts?size} Web Scripts
    </table>
    <br>
    <table>
      <tr><td><a href="${url.serviceContext}/index">Back to Web Scripts Home</a>
    </table>
    <br>
    <#macro recursepackage package>
       <#if package.scripts?size &gt; 0>
        <span class="mainSubTitle">Package: <a href="${url.serviceContext}/index/package${package.path}">${package.path}</a></span><br><br>
        <#list package.scripts as webscript>
        <#assign desc = webscript.description>
        <span class="mainSubTitle">${desc.shortName}</span>
        <table>
        <#list desc.URIs as uri>
          <tr><td><a href="${url.serviceContext}${uri}">${desc.method} ${url.serviceContext}${uri}</a>
        </#list>
          <tr><td>
        </table>
        <table>
          <#if desc.description??><tr><td>Description:<td>${desc.description}<#else></#if>
          <tr><td>Authentication:<td>${desc.requiredAuthentication}
          <tr><td>Transaction:<td>${desc.requiredTransaction}
          <tr><td>Format Style:<td>${desc.formatStyle}
          <tr><td>Default Format:<td>${desc.defaultFormat!"<i>Determined at run-time</i>"}
          <tr><td>
          <tr><td>Id:<td>${desc.id}
          <tr><td>Description:<td><a href="${url.serviceContext}/description/${desc.id}">${desc.storePath}/${desc.descPath}</a>
        </table>
        <br>
        </#list>
       </#if>
       <#list package.children as childpath>
          <@recursepackage package=childpath/>
       </#list>  
    </#macro>
    
    <@recursepackage package=rootpackage/>
  </body>
</html>