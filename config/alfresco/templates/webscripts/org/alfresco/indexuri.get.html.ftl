<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Index of Web Scripts URI '${uri.path}'</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
   <table>
     <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Index of Web Scripts URI '${uri.path}'</span></nobr></td>
     </tr>
     <tr><td><td>Alfresco ${server.edition} v${server.version}
     <tr><td><td>${uri.scripts?size} Web Scripts
    </table>
    <br>
    <table>
      <tr><td><a href="${url.serviceContext}/index">Back to Web Scripts Home</a>
      <#if uri.parent?exists>
         <tr><td><a href="${url.serviceContext}/index/uri${uri.parent.path}">Up to ${uri.parent.path}</a>
      </#if>
    </table>
    <br>
    <#if uri.children?size &gt; 0>
       <table>
          <@recurseuri uri=uri/>
       </table>
       <br>
    </#if>
    <#macro recurseuri uri>
       <#list uri.children as childpath>
          <#if childpath.scripts?size &gt; 0>
            <tr><td><a href="${url.serviceContext}/index/uri${childpath.path}">${childpath.path}</a>
          </#if>
          <@recurseuri uri=childpath/>
       </#list>  
    </#macro>
    <#list uri.scripts as webscript>
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
      <tr><td>Format Style:<td>${desc.formatStyle}
      <tr><td>
      <tr><td>Id:<td>${desc.id}
      <tr><td>Description:<td><a href="${url.serviceContext}/description/${desc.id}">${desc.storePath}/${desc.descPath}</a>
    </table>
    <br>
    </#list>
  </body>
</html>