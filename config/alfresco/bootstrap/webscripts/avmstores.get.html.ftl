<html>
  <head>
    <title>AVM Stores</title>
  </head>
  <body>
    AVM Stores
    <br>
    <br>
    <table>
     <tr>
<#list avm.stores as store>
     <tr>
        <td>${store.creator}<td>&nbsp;<td>${store.createdDate?datetime}<td>&nbsp;<td><a href="${url.serviceContext}/sample/avm/path/${store.id}/">${store.id}
     </tr>
</#list>
    </table>
  </body>
</html>
