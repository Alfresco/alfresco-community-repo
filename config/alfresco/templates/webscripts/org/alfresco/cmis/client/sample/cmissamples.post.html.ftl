<html>
<head>
  <title>CMIS Connection</title>
  <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
</head>
<body>
  <h1>Connection</h1>
  
  <#if error??>
  <h3 style="color:red">Error: ${error}</h3>
  <#else>
  <h3>Established!</h3>
  </#if>
  
  <div><a href="${url.service}">back</a></div>
  
  <hr/>
  
  <h2>Repository Info</h2>
  <table>
    <tr><td>Id:</td><td>${cmisSession.repositoryInfo.id?html}</td></tr>
    <tr><td>Name:</td><td>${cmisSession.repositoryInfo.name?html}</td></tr>
    <tr><td>Description:</td><td>${cmisSession.repositoryInfo.description?html}</td></tr>
    <tr><td>Vendor:</td><td>${cmisSession.repositoryInfo.vendorName?html}</td></tr>
    <tr><td>Product name:</td><td>${cmisSession.repositoryInfo.productName?html}</td></tr>
    <tr><td>Product version:</td><td>${cmisSession.repositoryInfo.productVersion?html}</td></tr>
  </table>

  <h2>Root folder</h2>
  <div>Root folder Id: ${cmisSession.rootFolder.id?html}</div>
    
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
    
  <h2>Base Types</h2>
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
