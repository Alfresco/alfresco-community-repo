<#import "cmis.lib.html.ftl" as cmisLib/>
<html>
  <@cmisLib.head>Delete</@cmisLib.head>
  <body>
    <div id="container">
      <@cmisLib.navigation>Connect</@cmisLib.navigation>    
      <div id="content">
      <#if error??>
        <h3 style="color: red;">${error?html}</h3>
      <#else>
        <#if needsConfirmation??>
          <h3>Delete ${object.name?html}?</h3>
          <table><tr>
          <td>
          <form action="<@cmisLib.cmisContextUrl conn.id/>/object" method="post">
            <input type="hidden" name="alf_method" value="delete">
            <input type="hidden" name="conn" value="${conn.id}">
            <input type="hidden" name="id" value="${object.id}">
            <input type="hidden" name="confirm" value="1">
            <@cmisLib.button "Yes"/>
          </form>
          </td><td>
          <form action="<@cmisLib.cmisContextUrl conn.id/>/object" method="get">
            <input type="hidden" name="conn" value="${conn.id}">
            <input type="hidden" name="id" value="${object.id}">
            <@cmisLib.button "No"/>
          </form>
          </td>
          </tr></table>
        <#else>
          <h3>${object.name?html} deleted!</h3>
        </#if>
      </#if>
      <#if conn??>
        <p><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${parent.id?url}">Return to folder</a></p>
      </#if>
     </div>
   </div>
  </body>
</html>
