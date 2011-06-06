<html>
<head>
  <title>CMIS Connection</title>
  <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
</head>
<body>
  <h1>Connection</h1>
  <table>
    <tr><td>Is default connection:</td><td>${connection.default?string}</td></tr>
    <tr><td>Is shared connection:</td><td>${connection.shared?string}</td></tr>
  </table>
  
  <h1>Repository Info</h1>
  <table>
    <tr><td>Id:</td><td>${cmisSession.repositoryInfo.id?html}</td></tr>
    <tr><td>Name:</td><td>${cmisSession.repositoryInfo.name?html}</td></tr>
    <tr><td>Description:</td><td>${cmisSession.repositoryInfo.description?html}</td></tr>
    <tr><td>Vendor:</td><td>${cmisSession.repositoryInfo.vendorName?html}</td></tr>
    <tr><td>Product name:</td><td>${cmisSession.repositoryInfo.productName?html}</td></tr>
    <tr><td>Product version:</td><td>${cmisSession.repositoryInfo.productVersion?html}</td></tr>
  </table>

  <h1>Root folder</h1>
  <span>Root folder Id: ${cmisSession.rootFolder.id?html}</span><br/><br/>
    
  <table>
    <tr>
      <th>Name</td>
      <th>Id</td>
      <th>Type</td>      
    </tr>
  <#list rootFolderChildren as obj>
    <tr>
      <td>${obj.name?html}</td>
      <td>${obj.id?html}</td>
      <td>${obj.type.id?html}</td>      
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
      <td>${bt.displayName?html}</td>
      <td>${bt.id?html}</td>    
    </tr>
  </#list>
  </table>
</body>
</html>
