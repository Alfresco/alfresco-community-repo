<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8">   
   <meta name="Generator" content="Alfresco Repository">
   <meta name="layoutengine" content="MSHTML"><style type="text/css">
      body {
         background-color:#FFFFFF;
         color:#000000;
         font-family:Verdana,Arial,sans-serif;
         font-size:11px;
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
		 margin: 17px;
		 text-decoration:underline;
      }
      table.links td, table.description td {
         border-bottom:1px dotted #555555;
         padding:5px;
      }
      table.description, table.links {
         border:0;
         border-collapse:collapse;
		 width:auto;
 		 margin:7px 20px 7px 20px;
      }
   </style>
</head>
<body>
<hr>
<h1> Документ (имя): ${document.name} </h1>
<hr>
<h2> Метаданные </h2>
<table class="description">
   <#if document.properties.title?exists>
                     <tr><td valign="top">Заголовок:</td><td> ${document.properties.title}</td></tr>
   <#else>
                     <tr><td valign="top">Заголовок:</td><td>&nbsp;</td></tr>
   </#if>
   <#if document.properties.description?exists>
                     <tr><td valign="top">Описание:</td><td> ${document.properties.description}</td></tr>
   <#else>
                     <tr><td valign="top">Описание:</td><td>&nbsp;</td></tr>
   </#if>
                     <tr><td>Создатель:</td><td> ${document.properties.creator}</td></tr>
                     <tr><td>Создано:</td><td> ${document.properties.created?datetime}</td></tr>
                     <tr><td>Модификатор:</td><td> ${document.properties.modifier}</td></tr>
                     <tr><td>Изменено:</td><td> ${document.properties.modified?datetime}</td></tr>
                     <tr><td>Размер:</td><td> ${document.size / 1024} КБ</td></tr>
</table>
<br>
<h2> Ссылки на контент </h2>
<table class="links">
   <tr>
   <td>Папка с контентом:</td><td> <a href="${contextUrl}/navigate/browse${document.parent.webdavUrl}">${contextUrl}/navigate/browse${document.parent.webdavUrl}</a></td>
   </tr>
   <tr>
   <td>URL-адрес контента:</td><td> <a href="${contextUrl}${document.url}">${contextUrl}${document.url}</a></td>
   </tr>
   <tr>
   <td>Загрузить с URL-адреса:</td><td> <a href="${contextUrl}${document.downloadUrl}">${contextUrl}${document.downloadUrl}</a></td>
   </tr>
   <tr>
   <td>URL-адрес WebDAV:</td><td> <a href="${contextUrl}${document.webdavUrl}">${contextUrl}${document.webdavUrl}</a></td>
   </tr>
</table>
</body>
</html>