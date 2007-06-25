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
     <#if status.exception?exists>
       <tr><td>&nbsp;     
       <@recursestack status.exception/>
     </#if>
     <tr><td><b>Server</b>:<td>Alfresco ${server.edition} v${server.version} schema ${server.schema}
     <tr><td><b>Time</b>:<td>${date?datetime}
    </table>
  </body>
</html>

<#macro recursestack exception>
   <#if exception.cause?exists>
      <@recursestack exception=exception.cause/>
   </#if>
   <tr><td><b>Exception:</b><td>${exception.class.name}<#if exception.message?exists> - ${exception.message}</#if>
   <tr><td><td>&nbsp;
   <#if exception.cause?exists == false>
      <#list exception.stackTrace as element>
         <tr><td><td>${element}
      </#list>  
      <#else>
         <tr><td><td>${exception.stackTrace[0]}
      </#if>
   <tr><td><td>&nbsp;
</#macro>
