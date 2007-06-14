<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco Javascript Debugger</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <form action="${url.match}" method="post">
        <input type="hidden" name="visible" value="<#if visible>false<#else>true</#if>">
        <table>
          <tr>
            <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
            <td><nobr><span class="mainTitle">Alfresco Javascript Debugger</span></nobr></td>
          </tr>
          <tr><td><td>Alfresco ${server.edition} v${server.version}
          <tr><td><td>Currently <#if visible>enabled<#else>disabled</#if>.
          <input type="submit" name="submit" value="<#if visible>Disable<#else>Enable</#if>">
        </table>
    </form>
  </body>
</html>