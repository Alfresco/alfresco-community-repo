<html>
<head>
  <title>${folder.name} - CMIS Browser</title>
  <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
</head>
<body>
  <div class="titlebar">
    <#if !folder.rootFolder><a href="${url.serviceContext}${url.match}?id=${folder.parentId?url}">UP</a> | </#if><a href="${url.serviceContext}${url.match}"></a>${folder.path?html!""}</td>
  </div>

  <table border="0">
  <#list children as child>
    <tr>

    <#if child.baseType.id == "cmis:folder">
    
      <td>
        <a href="${url.serviceContext}${url.match}?id=${child.id?url}"><img src="${url.context}/images/icons/folder_large.png"></a>
      </td>
      <td>
        <span class="title"><a href="${url.serviceContext}${url.match}?id=${child.id?url}">${child.name?html}</a></span><br/>
        <span>modified ${child.lastModificationDate.time?datetime} by ${child.lastModifiedBy?html}
      </td>

    <#else>

      <td>
        <#assign contentUrl = "${url.serviceContext}/cmis/content?id=${child.id?url}&conn=${connection.id?url}">
        <a target="_new" href="${contentUrl}">
        <#if child.renditions?? && (child.renditions?size > 0)>
          <#assign streamid = child.renditions?first.streamId>
        <#else>
          <#assign streamid = "">
        </#if>
        <#if streamid?length &gt; 0>
          <img src="${contentUrl}&stream=${streamid?url}">
        <#else>
          <img src="${url.context}/images/icons/file_large.gif">
        </#if>
        </a>
      </td>
      <td> 
        <span class="title"><a target="_new" href="${contentUrl}">${child.name?html}</a> (${child.versionLabel!""})</span><br/>
        <span class>modified ${child.lastModificationDate.time?datetime} by ${child.lastModifiedBy?html}<#if (child.contentStreamLength > -1)> / ${(child.contentStreamLength/1000)?int} KB</#if></span>
       </td>

    </#if>

    </tr>
  </#list>
  </table>
</body>
</html>