<html>
  <head>
    <title>CMIS Connection</title>
    <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
  </head>
  <body>
    <h1>Repository Info</h1>
    <table>
      <tr><td>Id:</td><td>${cmisSession.repositoryInfo.id}</td></tr>
      <tr><td>Name:</td><td>${cmisSession.repositoryInfo.name}</td></tr>
      <tr><td>Description:</td><td>${cmisSession.repositoryInfo.description}</td></tr>
      <tr><td>Vendor:</td><td>${cmisSession.repositoryInfo.vendorName}</td></tr>
      <tr><td>Product name:</td><td>${cmisSession.repositoryInfo.productName}</td></tr>
      <tr><td>Product version:</td><td>${cmisSession.repositoryInfo.productVersion}</td></tr>
    </table>

    <h1>Root folder</h1>
    Root folder Id: ${cmisSession.rootFolder.id}<br/><br/>
    
    <table>
      <tr>
        <th>Name</td>
        <th>Id</td>
        <th>Type</td>      
      </tr>
    <#list rootFolderChildren as obj>
      <tr>
        <td>${obj.name}</td>
        <td>${obj.id}</td>
        <td>${obj.type.id}</td>      
      </tr>
    </#list>
    </table>
    
    <h1>Base Types</h1>
    <table>
      <tr>
        <th>Name</td>
        <th>Id</td> 
      </tr>
    <#list baseTypes as bt>
      <tr>
        <td>${bt.displayName}</td>
        <td>${bt.id}</td>    
      </tr>
    </#list>
    </table>
  </body>
</html>
