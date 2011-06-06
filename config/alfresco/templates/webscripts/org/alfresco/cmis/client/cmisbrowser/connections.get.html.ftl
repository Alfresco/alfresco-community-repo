<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head>Connections</@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.navigation>Connections</@cmisLib.navigation>
  
    <div id="content">
    <table class="details">
      <tr><th colspan="9"><b>My Connections</b></th></tr>
    <#if connections?size == 0>
      <tr><td colspan="9">None</td></tr>
    </table>
    <#else>
      <tr><td><b>Server</b></td><td></td><td><b>Repository</b></td><td><b>Connection</b></td><td><b>User</b><td><td></td><td></td><td></td></tr>
    <#list connections as conn>
      <tr>
        <td>${conn.server.name!""}</td>
        <td>${conn.server.description!""}</td>
        <td>${conn.session.repositoryInfo.name}</td>
        <td>${conn.id}</td>
        <td>${conn.userName}</td>
        <td><a href="<@cmisLib.cmisContextUrl conn.id/>/repo">info</a></td>
        <td><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${conn.session.rootFolder.id}">browse</a></td>
        <td><#if conn.supportsQuery()><a href="<@cmisLib.cmisContextUrl conn.id/>/query">query</a></#if></td>
        <td><form action="<@cmisLib.cmisContextUrl conn.id/>" method="post"><input type="hidden" name="alf_method" value="delete"><@cmisLib.button "disconnect"/></form></td>
      </tr>
    </#list>
    </table>
    <br>
    <table>
      <tr><td><a href="<@cmisLib.cmisContextUrl/>/federatedquery">query all</a></td></tr>
    </table>
    </#if>
    <br>
    <table class="details">
      <tr><th colspan="3"><b>Available CMIS Servers</b></th></tr>
    <#if servers?size == 0>
      <tr><td colspan="3">None</td></tr>
    <#else>
      <tr><td><b>Server</b></td><td></td><td></td></tr>
    <#list servers as server>
      <tr>
        <td>${server.name!""}</td>
        <td>${server.description!""}</td>
        <td><form action="<@cmisLib.cmisContextUrl/>/connect" method="post"><input type="hidden" name="server" value="${server.name}"/><@cmisLib.button "connect"/></form></td>
      </tr>
    </#list>
    </#if>
    </table>

    </div>
  </div>
  </body>
</html>
