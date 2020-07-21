<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Upload Web Script Sample</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="mainTitle">Upload Web Script Sample</span></nobr></td>
      </tr>
      <tr><td><td>Alfresco ${server.edition} v${server.version}
      <tr><td><td>&nbsp;
      <tr><td><td>Uploaded <a href="${url.serviceContext}/sample/folder${upload.displayPath}">${upload.name}</a> of size ${upload.properties.content.size}.
    </table>
  </body>
</html>
