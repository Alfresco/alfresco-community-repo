<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Web Script Status ${status.code} - ${status.codeName}</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
     <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Web Script Status ${status.code} - ${status.codeName}</span></nobr></td>
     </tr>
    </table>
    <br>
    <table>
     <tr><td>The Web Script <a href="${url.full}">${url.service}</a> has responded with a status of ${status.code} - ${status.codeName}.
    </table>
    <br>
    <table>
     <tr><td><b>${status.code} Description:</b><td> ${status.codeDescription}
     <tr><td>&nbsp;
     <tr><td><b>Message:</b><td>${status.message!"<i>&lt;Not specified&gt;</i>"}
     <tr><td><b>Exception:</b><td><#if status.exception?exists>${status.exception.class.name}<#if status.exception.message?exists> - ${status.exception.message}</#if></#if>
     <#if status.exception?exists>
       <tr><td><td>&nbsp;
       <#list status.exception.stackTrace as element>
         <tr><td><td>${element}
       </#list>  
     </#if>
    </table>
  </body>
</html>
