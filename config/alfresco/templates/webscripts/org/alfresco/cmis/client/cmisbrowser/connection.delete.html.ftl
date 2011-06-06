<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head>Disconnected Connection</@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.navigation>${conn.id} disconnected</@cmisLib.navigation>
  </div>
  </body>
</html>
