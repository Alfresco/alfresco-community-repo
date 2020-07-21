<html>
  <head>
    <title>${folder.displayPath}/${folder.name}</title>
  </head>
  <body>
    Folder: ${folder.displayPath}/${folder.name}
    <br>
    <table>
     <#if folder.parent.parent?exists>
     <tr>
       <td><td><a href="${url.serviceContext}/sample/folder<@encodepath node=folder.parent/>">..</a>
     </tr>
     </#if>
<#list folder.children as child>
     <tr>
       <#if child.isContainer>
         <td>&gt;<td><a href="${url.serviceContext}/sample/folder<@encodepath node=child/>">${child.name}</a>
       <#else>
         <td><td><a href="${url.serviceContext}/api/node/content/${child.nodeRef.storeRef.protocol}/${child.nodeRef.storeRef.identifier}/${child.nodeRef.id}/${child.name?url}">${child.name}</a>
       </#if>
     </tr>
</#list>
    </table>
  </body>
</html>

<#macro encodepath node><#if node.parent?exists><@encodepath node=node.parent/>/${node.name?url}</#if></#macro>