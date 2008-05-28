<html>
 <head> 
   <title>Upload Web Script Sample</title> 
   <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
 </head>
 <body>
   <table>
     <tr>
       <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
       <td><nobr>Upload Web Script Sample</nobr></td>
     </tr>
     <tr><td><td>Alfresco ${server.edition} v${server.version}
     <tr><td><td> 
     <tr><td><td>Uploaded <a href="${url.serviceContext}/sample/folder${upload.displayPath}">${upload.name}</a> of size ${upload.properties.content.size}.
   </table>
 </body>
</html>