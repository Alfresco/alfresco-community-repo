<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head><#if isDoc>Document ${object.name}<#else>Folder ${object.path}</#if></@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.connectionNavigation conn><#if isDoc>Document ${object.name}<#else>Folder ${object.path}</#if> details</@cmisLib.connectionNavigation>
    <div id="content">
    
    <#if parent?exists>
    <table>
      <tr><td><a href="<@cmisLib.cmisContextUrl conn.id/>/folder?id=${parent.id}">Parent Folder ${parent.path}</a></td></tr>
    </table>
    <br>
    </#if>

    <table><tr>

    <#if isDoc>
      <#if object.contentStreamLength?? && object.contentStreamLength gt 0>
        <td>
        <#if object.contentStreamMimeType??>
          <#assign mimetype = " (${object.contentStreamMimeType?html})">
        <#else>
          <#assign mimetype = "">
        </#if>
        <form action="${url.serviceContext}/cmis/content" method="get">
          <input type="hidden" name="conn" value="${conn.id}">
          <input type="hidden" name="id" value="${object.id}">
          <@cmisLib.button "View Content${mimetype}"/>
        </form>      
        </td>
      </#if>      
    <#elseif isFolder>
      <td>
      <form action="<@cmisLib.cmisContextUrl conn.id/>/folder" method="get">
        <input type="hidden" name="id" value="${object.id}">
        <@cmisLib.button "View Children"/>
      </form>      
      </td>
    </#if>

    <td>
    <form action="<@cmisLib.cmisContextUrl conn.id/>/object" method="post">
      <input type="hidden" name="alf_method" value="delete">
      <input type="hidden" name="conn" value="${conn.id}">
      <input type="hidden" name="id" value="${object.id}">
      <@cmisLib.button "Delete"/>
    </form>
    </td>

    </tr></table>

    <#assign renditions = object.renditions![]>
    <#if renditions?size gt 0>
    <table class="details">
      <tr><th colspan="3"><b>Renditions</b></th></tr>
      <tr><td><b>Kind</b></td><td><b>Mimetype</b></td><td><b>Length</b></td></tr>
      <#list renditions as rendition>
      <tr>
      <td><a href="${url.serviceContext}/cmis/content?id=${object.id?url}&conn=${conn.id?url}&stream=${rendition.streamId?url}">${rendition.kind?html}</a></td>
      <td>${rendition.mimeType}</td>
      <td><#if rendition.length != -1>${rendition.length?c}</#if></td>
      </tr>
      </#list>
    </table>
    <br>
    </#if>

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
    <br>
    
    <table class="details">
      <tr><th colspan="3"><b>Relationships</b></th></tr>
      <tr><td><b>Id</b></td><td><b>Name</b></td><td><b>Related Objects</b></td></tr>
      <#assign relationships = object.relationships![]>
      <#if relationships??>
          <#list relationships as relationship>
              <tr>
                <td>${relationship.id?html}</td>
                <td>${relationship.name?html}</td>
                <td><a href="<@cmisLib.cmisContextUrl conn.id/>/object?id=${relationship.source.id?url}">${relationship.source.name?html}</a> &rarr;
                <a href="<@cmisLib.cmisContextUrl conn.id/>/object?id=${relationship.target.id?url}">${relationship.target.name?html}</a></td>
              </tr>
          </#list>
      </#if>
    </table>
    
    </div>
  </div>
  </body>
</html>
