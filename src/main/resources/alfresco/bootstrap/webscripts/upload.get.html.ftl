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
    </table>
    <p>
    <table>
      <form action="${url.service?html}" method="post" enctype="multipart/form-data" charset="utf-8">
        <tr><td>File:<td><input type="file" name="file">
        <tr><td>Title:<td><input name="title">
        <tr><td>Description:<td><input name="desc">
        <tr><td><td>
        <tr><td><td><input type="submit" name="submit" value="Upload">
      </form>
    </table>
  </body>
</html>
