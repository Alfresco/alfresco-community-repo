<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head>Repository: ${repoinfo.name}</@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.connectionNavigation conn>Information</@cmisLib.connectionNavigation>

    <div id="content">
    <table>
        <tr><td class="headercol">Name:</td><td>${repoinfo.name}</td></tr>
        <tr><td class="headercol">Id:</td><td>${repoinfo.id}<td></tr>
        <tr><td class="headercol">Product Name:</td><td>${repoinfo.productName}<td></tr>
        <tr><td class="headercol">Product Version:</td><td>${repoinfo.productVersion}<td></tr>
        <tr><td class="headercol">Vendor:</td><td>${repoinfo.vendorName}<td></tr>
        <tr><td class="headercol">CMIS Version:</td><td>${repoinfo.cmisVersionSupported}<td></tr>
    </table>
    <br>
    <table>
        <tr>
          <td>You may <a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${rootFolder.id?url}">browse the ${rootFolder.name?html} folder</a> 
              or <a href="<@cmisLib.cmisContextUrl conn.id/>/query">query</a> the repository.</td>
        </tr>
    </table>
    </div>
  </body>
</html>
