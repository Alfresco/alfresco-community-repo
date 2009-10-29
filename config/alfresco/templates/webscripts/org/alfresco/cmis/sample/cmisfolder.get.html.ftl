<html>
  <head>
    <title>${folder.title} Folder</title>
    <link rel="stylesheet" type="text/css" href="${url.context}/themes/default/base.css" />
  </head>
  <body>
    <table>
      <tr><td><a href="${url.serviceContext}/sample/cmis/repo">CMIS Repository</a>: ${folder.title} folder</td></tr>
    </table>
    <br>
    <table>
<#if folder.getLinks("cmis-parent")?first?exists>
      <tr>
       <td>d</td><td></td><td></td><td></td>
       <td><a href="${url.serviceContext}${folder.getLinks("cmis-parent")?first.href?replace(".*/api/", url.match, "r")}">..</a></td>
      </tr>
</#if>
<#list children.entries as entry>
      <#assign cmis_object=entry.getExtension(atom.names.cmis_object)>
      <tr>
        <#if cmis_object.baseType.value == "folder">
          <td>d</td>
          <td>${cmis_object.lastModifiedBy.value}</td>
          <td></td>
          <td>${cmis_object.lastModificationDate.dateValue?datetime}</td>
          <td><a href="${url.serviceContext}${entry.selfLink.href?replace(".*/api/", url.match, "r")}">${cmis_object.name.value}</a></td>
        <#else>
          <td></td>
          <td>${cmis_object.lastModifiedBy.value}</td>
          <td>${cmis_object.contentStreamLength.integerValue?string}</td>
          <td>${cmis_object.lastModificationDate.dateValue?datetime}</td>
          <td><a target="_new" href="${url.context}${entry.contentSrc?replace(".*/api/", "/proxy/alfresco/api/", "r")}">${cmis_object.name.value}</a></td>
        </#if>
      </tr>
</#list>
    </table>
  </body>
</html>
