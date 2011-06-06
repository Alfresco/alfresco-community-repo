<#import "cmis.lib.html.ftl" as cmisLib/>
<html>
  <@cmisLib.head>Connect</@cmisLib.head>
  <body>
    <div id="container">
      <@cmisLib.navigation>Create</@cmisLib.navigation>    
      <div id="content">
      <#if error??>
        <h3 style="color: red;">${error?html}</h3>
      <#else>
        <h3>${object.name?html} created!</h3>
        <table class="details">
          <#assign properties = object.properties>
          <tr><th colspan="4"><b>Properties</b></th></tr>
          <tr><td><b>Id</b></td><td><b>Type</b></td><td><b>Cardinality</b></td><td><b>Value(s)</b></td></tr>
          <#list properties as property>
          <tr>
            <td>${property.id?html}</td>
            <td>${property.type?capitalize}</td>
            <td><#if property.isMultiValued()>multiple</#if></td>
          <td><@cmisLib.propertyvalue property conn.id>,<br></@cmisLib.propertyvalue></td>
          </tr>
          </#list>
        </table>
      </#if>
        <p><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${parent.id?url}">Return to folder</a></p>
     </div>
   </div>
  </body>
</html>