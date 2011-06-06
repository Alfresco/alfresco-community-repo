<html>
<head>
  <title>CMIS Create Folder</title>
  <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
</head>
<body>
  <h1>New Folder</h1>
  <#if folder??>
  
  <table>
    <tr><td>Name:</td><td>${folder.name}</td></tr>
    <tr><td>Path:</td><td>${folder.path}</td></tr>
    <tr><td>Id:</td><td>${folder.id}</td></tr>
    <tr><td>Type:</td><td>${folder.type.id}</td></tr>
    <tr><td>Created by:</td><td>${folder.createdBy}</td></tr>
    <tr><td>Creation date:</td><td>${folder.creationDate.time?datetime}</td></tr>
  </table>
  
  <#else>
  
  <form action="${url.service}" method="get" charset="utf-8">
  <table>
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
      <td><input type="submit" name="submit" value="Create Folder"/></td>
    </tr>
  </table>
  </form>
  
  </#if>
</body>
</html>