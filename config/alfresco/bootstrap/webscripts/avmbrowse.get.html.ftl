<html>
  <head>
    <title>AVM Folder: ${folder.displayPath}</title>
  </head>
  <body>
    <a href="${url.serviceContext}/sample/avm/stores">AVM Store</a>: ${store.id}
    <br>
    <br>
    AVM Folder: ${folder.displayPath}
    <br>
    <br>
    <table>
     <#if folder.parent?exists>
     <tr>
       <td>${folder.parent.properties.creator}<td>&nbsp;<td>&nbsp<td>&nbsp;<td>${folder.parent.properties.modified?datetime}<td>&nbsp;<td><td><a href="${url.serviceContext}/sample/avm/path/${store.id}<#if folder.parent.parent?exists><@encodepath node=folder.parent/><#else>/</#if>">..</a>
     </tr>
     </#if>
<#list folder.children as child>
     <tr>
       <#if child.isContainer>
         <td>${child.properties.creator}<td>&nbsp;<td>&nbsp<td>&nbsp;<td>${child.properties.modified?datetime}<td>&nbsp;<td>&gt;<td><a href="${url.serviceContext}/sample/avm/path/${store.id}<@encodepath node=child/>">${child.name}</a>
       <#else>
         <td>${child.properties.creator}<td>&nbsp;<td>${child.size}<td>&nbsp;<td>${child.properties.modified?datetime}<td>&nbsp;<td><td><a href="${url.serviceContext}/api/node/content/${child.nodeRef.storeRef.protocol}/${child.nodeRef.storeRef.identifier}/${child.nodeRef.id}/${child.name?url}">${child.name}</a>
       </#if>
     </tr>
</#list>
    </table>
  </body>
</html>

<#macro encodepath node><#if node.parent?exists><@encodepath node=node.parent/>/${node.name?url}</#if></#macro>