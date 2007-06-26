<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Web Script: ${script.id}</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css"/>
  </head>
  <body>
    <table>
     <tr><td><nobr><span class="mainTitle">Web Script: ${script.id}</span></nobr></td></tr>
     <tr><td>Alfresco ${server.edition} v${server.version}</td></tr>
     <tr><td>Generated from ${url.full} on ${date?datetime}</td></tr>
    </table>
	<p/>
    <table>
     <span class="mainSubTitle">Script Properties</span>
     <tr><td>Id:</td><td>${script.id}</td></tr>
     <tr><td>Short Name:</td><td>${script.shortName}</td></tr>
     <tr><td>Description:</td><td>${script.description}</td></tr>
     <tr><td>Authentication:</td><td>${script.requiredAuthentication}</td></tr>
     <tr><td>Transaction:</td><td>${script.requiredTransaction}</td></tr>
     <tr><td>Method:</td><td>${script.method}</td></tr>
     <#list script.URIs as URI>
     <tr><td>URL Template:</td><td>${URI}</td></tr>
     </#list>
     <tr><td>Format Style:</td><td>${script.formatStyle}</td></tr>
     <tr><td>Default Format:</td><td>${script.defaultFormat}</td></tr>
     <tr><td>Implementation:</td><td>${script_class}</td></tr>
    </table>
    <p/>
	<#list stores as store>
        <span class="mainSubTitle">Store: ${store.path}</span>
        <p/>
        <table>
        <#if store.files?size == 0>
          <tr><td>[No implementation files]</td></tr>
        <#else>
          <p/>
          <#list store.files as file>
            <tr><td><span class="mainSubTitle">File: ${file.path}</span> <#if file.overridden>[overridden]</#if></td></tr>
            <tr><td><pre>${file.content?html}</pre></td></tr>
          </#list>
        </#if>
        </table>
        <p/>
    </#list>

    <!-- Web Script meta-data -->
    <span style="display:none">
    <webscript scriptid="${script.id}" xmlns="http://www.alfresco.org/webscript/1.0">
    <#list stores as store>
      <#list store.files as file>
        <file path="${file.path}" store="${store.path}"><![CDATA[${file.content}]]></file>
      </#list>
    </#list>
    </webscript>
    </span>
    <!-- End: Web Script meta-data -->
    
  </body>
</html>