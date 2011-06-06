<html>
<head>
  <title>Upload Web Script Sample</title>
  <link rel="stylesheet" href="${url.context}/css/base.css" TYPE="text/css">
</head>
<body>
  <h1>New Document</h1>
    <table>
      <tr><td>Name:</td><td>${doc.name?html}</td></tr>
      <tr><td>Id:</td><td>${doc.id?html}</td></tr>
      <tr><td>Type:</td><td>${doc.type.id?html}</td></tr>
      <tr><td>Created by:</td><td>${doc.createdBy?html}</td></tr>
      <tr><td>Creation date:</td><td>${doc.creationDate.time?datetime}</td></tr>
      <tr><td>Size:</td><td>${(doc.contentStreamLength/1024)?int} KB</td></tr>
      <tr><td>MIME type:</td><td>${doc.contentStreamMimeType?html}</td></tr>
    </table>
  </body>
</html>
