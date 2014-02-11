<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8">   
   <meta name="Generator" content="Alfresco Repository"><style type="text/css">
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
<h1> 文档（名称）：  ${document.name} </h1>
<hr/>
<fieldset>
<legend> 元数据 </legend>
<table class="description">
   <#if document.properties.title?exists>
                     <tr><td valign="top">标题：</td><td> ${document.properties.title}</td></tr>
   <#else>
                     <tr><td valign="top">标题：</td><td>&nbsp;</td></tr>
   </#if>
   <#if document.properties.description?exists>
                     <tr><td valign="top">说明：</td><td> ${document.properties.description}</td></tr>
   <#else>
                     <tr><td valign="top">说明：</td><td>&nbsp;</td></tr>
   </#if>
                     <tr><td>创建者：</td><td> ${document.properties.creator}</td></tr>
                     <tr><td>创建时间：</td><td> ${document.properties.created?datetime}</td></tr>
                     <tr><td>修改者：</td><td> ${document.properties.modifier}</td></tr>
                     <tr><td>修改时间：</td><td> ${document.properties.modified?datetime}</td></tr>
                     <tr><td>大小：</td><td> ${document.size / 1024} KB</td></tr>
</table>
</fieldset>
<fieldset>
<legend> 内容链接 </legend>
<table class="links">
   <tr>
   <td>内容文件夹：</td><td> <a href="${shareContextUrl}/page/site/${parentPathFromSites}">${shareContextUrl}/page/site/${parentPathFromSites}</a></td>
   </tr>
   <tr>
   <td>内容 URL：</td><td> <a href="${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}">${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}</a></td>
   </tr>
   <tr>
   <td>下载 URL：</td><td> <a href="${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true">${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true</a></td>
   </tr>
</table>
</fieldset>
</body>
</html>