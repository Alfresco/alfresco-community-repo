<html>
<head> 
  <title>CMIS Upload Web Script Sample</title> 
  <link rel="stylesheet" href="${url.context}/css/base.css" TYPE="text/css">
</head>
<body>
  <h1>Upload</h1>
  <form action="${url.service}" method="post" enctype="multipart/form-data" charset="utf-8">
  <table>
    <tr>
      <td>File:</td>
      <td><input type="file" name="file" size="100"/></td>
    </tr> 
    <tr>
      <td>Name:</td>
      <td><input type="text" name="name" size="100"/></td>
    </tr> 
    <tr>
      <td>Path:</td>
      <td><input type="text" name="path" value="/" size="100"/></td>
    </tr> 
    <tr>
      <td></td>
      <td><input type="submit" name="submit" value="Upload"/></td>
    </tr>
  </table>
  </form>
</body>
</html>
