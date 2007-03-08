<html>
  <body>
    <img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
    Category search: ${args.c}
    <br>
    <table>
<#list resultset as node>
     <tr>
       <td><img src="${url.context}${node.icon16}"/>
       <td><a href="${url.context}${node.url}">${node.name}</a>
     </tr>
</#list>
    </table>
  </body>
</html>