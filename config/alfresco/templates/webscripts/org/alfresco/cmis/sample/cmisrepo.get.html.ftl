<html>
  <head>
    <title>Repository: ${repo.name}</title>
    <link rel="stylesheet" type="text/css" href="${url.context}/themes/default/base.css" />
  </head>
  <body>
    <table>
      <tr><td>CMIS Repository</td></tr>
    </table>
    <br>
    <table>
        <tr><td>Name:</td><td>${repo.name} (<a href="${url.serviceContext}${repo.rootFolderId?replace(".*/api", "/sample/cmis/folder", "r")}">Root Folder</a>)</td></tr>
        <tr><td>Id:</td><td>${repo.id}<td></tr>
        <tr><td>Product Name:</td><td>${repo.productName}<td></tr>
        <tr><td>Product Version:</td><td>${repo.productVersion}<td></tr>
        <tr><td>Vendor:</td><td>${repo.vendorName}<td></tr>
        <tr><td>CMIS Version:</td><td>${repo.versionsSupported}<td></tr>
    </table>
  </body>
</html>
