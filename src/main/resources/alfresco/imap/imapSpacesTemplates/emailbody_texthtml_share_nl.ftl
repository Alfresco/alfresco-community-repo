<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">   
   <meta name="Generator" content="Alfresco Repository">

   <style type="text/css">
      body {
         background-color:#FFFFFF;
         color:#000000;
      }
      * {
         font-family:Verdana,Arial,sans-serif;
         font-size:11px;
      }
      h1 {
         text-align:left;
         font-size:15px;
      }
      h2 {
         text-align:left;
         font-size:13px;
      }
      fieldset {
         border:1px dotted #555555;
         margin:15px 5px 15px 5px;
      }
      fieldset legend {
         font-weight:bold;
         border:1px dotted #555555;
         background-color: #FFFFFF;
         padding:7px;
      }
      .links {
         border:0;
         border-collapse:collapse;
         width:99%;
      }
      .links td {
         border:0;
         padding:5px;
      }
      .description {
         border:0;
         border-collapse:collapse;
         width:99%;
      }
      .description td {
         /*border:1px dotted #555555;*/
         padding:5px;
      }
      #start_workflow input, #start_workflow select, #start_workflow textarea
      {
         border:1px solid #555555;
      }
   </style>
</head>
<body>
<hr/>
<h1> Document(naam): ${document.name?html} </h1>
<hr/>
<fieldset>
<legend> Metagegevens </legend>
<table class="description">
   <#if document.properties.title?exists>
                     <tr><td valign="top">Titel:</td><td> ${document.properties.title?html}</td></tr>
   <#else>
                     <tr><td valign="top">Titel:</td><td>&nbsp;</td></tr>
   </#if>
   <#if document.properties.description?exists>
                     <tr><td valign="top">Beschrijving:</td><td> ${document.properties.description?html}</td></tr>
   <#else>
                     <tr><td valign="top">Beschrijving:</td><td>&nbsp;</td></tr>
   </#if>
                     <tr><td>Maker:</td><td> ${document.properties.creator?html}</td></tr>
                     <tr><td>Gemaakt:</td><td> ${document.properties.created?datetime}</td></tr>
                     <tr><td>Wijziger:</td><td> ${document.properties.modifier?html}</td></tr>
                     <tr><td>Gewijzigd:</td><td> ${document.properties.modified?datetime}</td></tr>
                     <tr><td>Grootte:</td><td> ${document.size / 1024} kB</td></tr>
</table>
</fieldset>
<fieldset>
<legend> Contentkoppelingen </legend>
<table class="links">
   <tr>
   <td>Contentmap:</td><td> <a href="${contentFolderUrl?html}">${contentFolderUrl?html}</a></td>
   </tr>
   <tr>
   <td>Content-URL:</td><td> <a href="${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}">${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}</a></td>
   </tr>
   <tr>
   <td>Download-URL:</td><td> <a href="${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true">${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true</a></td>
   </tr>
</table>
</fieldset>
</body>
</html>