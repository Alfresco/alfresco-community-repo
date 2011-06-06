<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head>${folder.name?html} Folder</@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.connectionNavigation conn>Folder ${folder.name?html} <a href="<@cmisLib.cmisContextUrl conn.id/>/object?id=${folder.id?url}">details</a></@cmisLib.connectionNavigation>
    <div id="content">

    <table id="directory-list">
<#if folderParent?exists>
      <tr>
       <td colspan="5"><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${folderParent.id?url}"><@cmisLib.image "up"/></a></td>
      </tr>
</#if>
<#list folderChildren as child>
      <tr>
        <#if child.baseType.id == "cmis:folder">
          <td><@cmisLib.objectImage child.baseType.id/></td>
          <td><#if child.lastModifiedBy??>${child.lastModifiedBy?html}</#if></td>
          <td></td>
          <td><#if child.lastModificationDate??>${child.lastModificationDate.time?datetime}</#if></td>
          <td><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${child.id?url}">${child.name?html}</a></td>
        <#else>
          <td><@cmisLib.objectImage child.baseType.id?html/></td>
          <td><#if child.lastModifiedBy??>${child.lastModifiedBy?html}</#if></td>
          <td style="text-align: right;"><#if child.contentStreamLength??>${child.contentStreamLength?c}</#if></td>
          <td><#if child.lastModificationDate??>${child.lastModificationDate.time?datetime}</#if></td>
          <td><a href="<@cmisLib.cmisContextUrl conn.id/>/object?id=${child.id?url}">${child.name?html}</a></td>
        </#if>
      </tr>
</#list>
    </table>
    
    <hr/>
    
    <form action="<@cmisLib.cmisContextUrl conn.id/>/folder" method="post">
    <input type="hidden" name="conn" value="${conn.id}">
    <input type="hidden" name="parent" value="${folder.id}">
    <input type="hidden" name="type" value="folder">
    <table class="details">
      <tr><th colspan="2">Create new folder</th></tr>
      <tr><td>Name:</td><td><input type="text" name="name" size="60"/></td></tr> 
      <tr><td>Object Type:</td><td><input type="text" name="objectType" value="cmis:folder" size="60"/></td></tr> 
      <tr><td></td><td><@cmisLib.button "create"/></td></tr> 
    </table>
    </form>
    
    <hr/>
    
    <form action="<@cmisLib.cmisContextUrl conn.id/>/folder" method="post" enctype="multipart/form-data">
    <input type="hidden" name="conn" value="${conn.id}">
    <input type="hidden" name="parent" value="${folder.id}">
    <input type="hidden" name="type" value="document">
    <table class="details">
      <tr><th colspan="2">Upload document</th></tr>
      <tr><td>File:</td><td><input type="file" name="file" size="60"/></td></tr> 
      <tr><td>Name:</td><td><input type="text" name="name" size="60"/></td></tr> 
      <tr><td>Object Type:</td><td><input type="text" name="objectType" value="cmis:document" size="60"/></td></tr> 
      <tr><td></td><td><@cmisLib.button "upload"/></td></tr> 
    </table>
    </form>

    </div>
  </div>
  </body>
</html>
