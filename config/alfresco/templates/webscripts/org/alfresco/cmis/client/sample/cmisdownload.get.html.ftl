<html>
<head>
  <title>CMIS Document Download</title>
  <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
</head>
<body>
  <h1>Document</h1>
  
  <#if doc??>
  
  <table>
    <tr><td>Name:</td><td>${doc.name?html}</td></tr>
    <tr><td>Id:</td><td>${doc.id?html}</td></tr>
    <tr><td>Type:</td><td>${doc.type.displayName?html} (${doc.type.id?html})</td></tr>
    <tr><td>Created by:</td><td>${doc.createdBy?html}</td></tr>
    <tr><td>Creation date:</td><td>${doc.creationDate.time?datetime}</td></tr>
    <tr><td>Last modified by:</td><td>${doc.lastModifiedBy?html}</td></tr>
    <tr><td>Last modification date:</td><td>${doc.lastModificationDate.time?datetime}</td></tr>    
    <tr><td>Size:</td><td>${doc.contentStreamLength?c} bytes</td></tr>
    <tr><td>MIME type:</td><td>${doc.contentStreamMimeType?html}</td></tr>
    <tr><td>Version label:</td><td>${doc.versionLabel!""?html}</td></tr>
    <tr><td></td><td><a href="${url.serviceContext}/cmis/content?id=${doc.id?url}&conn=${connection.id?url}">download</a></td></tr>
  </table>
    
  <#else>
  
  <form action="${url.service}" method="get" charset="utf-8">
  <table>
    <tr>
      <td>Path:</td>
      <td><input type="text" name="path" value="/" size="100"/></td>
    </tr> 
    <tr>
      <td></td>
      <td><input type="submit" name="submit" value="Get document"/></td>
    </tr>
  </table>
  </form>
  
  </#if>
</body>
</html>