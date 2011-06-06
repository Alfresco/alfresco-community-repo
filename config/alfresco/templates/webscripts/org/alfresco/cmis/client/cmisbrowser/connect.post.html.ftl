<#import "cmis.lib.html.ftl" as cmisLib/>
<html>
  <@cmisLib.head>Connect</@cmisLib.head>
  <body>
    <div id="container">
      <@cmisLib.navigation>Connect</@cmisLib.navigation>    
      <div id="content">
  		<table class="details">
  		  <tr><th colspan="3">${serverDefinition.name?html} (${serverDefinition.description?html})</th></tr> 
        <#if error??>
        <form action="${url.service}" method="post">  		
            <input type="hidden" name="server" value="${serverDefinition.name}">
            <tr><td>Error:</td><td style="color:red">${error?html}</td></tr>
            <tr><td>Username:</td><td><input type="text" name="username" size="30"/></td></tr>
            <tr><td>Password:</td><td><input type="password" name="password" size="30"/></td></tr>
            <tr><td>Repository Id:</td><td><input type="text" name="repositoryid" size="30"/> (leave empty for the first repository)</td></tr>
            <tr><td></td><td><@cmisLib.button "connect"/></td></tr>
        </form>
        <#else>
        <table>
          <tr><td class="headercol">Name:</td><td>${repoinfo.name}</td></tr>
          <tr><td class="headercol">Id:</td><td>${repoinfo.id}<td></tr>
          <tr><td class="headercol">Product Name:</td><td>${repoinfo.productName}<td></tr>
          <tr><td class="headercol">Product Version:</td><td>${repoinfo.productVersion}<td></tr>
          <tr><td class="headercol">Vendor:</td><td>${repoinfo.vendorName}<td></tr>
          <tr><td class="headercol">CMIS Version:</td><td>${repoinfo.cmisVersionSupported}<td></tr>
          <tr><td></td><td><a href="<@cmisLib.cmisContextUrl/>/connections">Return to the connection page</a></td></tr>
          <tr><td></td><td><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${rootFolder.id?url}">Browse the root folder "${rootFolder.name?html}"</a></td></tr>
          <tr><td></td><td><a href="<@cmisLib.cmisContextUrl conn.id/>/query">Query the repository</a></td></tr>
        </#if>
        </table>
     </div>
   </div>
  </body>
</html>