<html>
<head>
  <title>CMIS Client Samples</title>
  <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
</head>
<body>
  <h1>CMIS Client Samples</h1>

  <ul>
    <li><a href="${url.service}/current">Current CMIS connection</a></li>
    <li><a href="${url.service}/servers">Preconfigured servers</a></li>
    <li><a href="${url.service}/browser">Simple CMIS browser</a></li>
    <li><a href="${url.service}/createfolder">Create folder example</a></li>
    <li><a href="${url.service}/download">Download document example</a></li>
    <li><a href="${url.service}/upload">Upload document example</a></li>
  </ul>
  <br/><br/>
    
  <div class="titlebar">Current CMIS Connection</div>
  <table>
    <tr><td>Id:</td><td>${cmisSession.repositoryInfo.id?html}</td></tr>
    <tr><td>Name:</td><td>${cmisSession.repositoryInfo.name?html}</td></tr>
    <tr><td>Description:</td><td>${cmisSession.repositoryInfo.description?html}</td></tr>
    <tr><td>Vendor:</td><td>${cmisSession.repositoryInfo.vendorName?html}</td></tr>
    <tr><td>Product name:</td><td>${cmisSession.repositoryInfo.productName?html}</td></tr>
    <tr><td>Product version:</td><td>${cmisSession.repositoryInfo.productVersion?html}</td></tr>
  </table>
  <br/><br/>
  
  <div class="titlebar">Select CMIS Connection</div>
  <table>
    <tr>
      <td style="vertical-align:text-top;">Local Alfresco server:</td>
      <td>
        <form action="${url.service}" method="post">
          <input type="hidden" name="type" value="local"/>
          <input type="submit" value="Connect"/>
        </form>
        <br/><br/>
      </td>
    </tr>
    <tr>
      <td style="vertical-align:text-top;">Custom OpenCMIS configuration:</td>
      <td>
        <form action="${url.service}" method="post">
          <textarea name="config" cols="100" rows="10">
org.apache.chemistry.opencmis.binding.spi.type=atompub
org.apache.chemistry.opencmis.binding.atompub.url=http://cmis.alfresco.com/service/cmis
org.apache.chemistry.opencmis.user=admin
org.apache.chemistry.opencmis.password=admin
          </textarea>
          <br/>
          <input type="hidden" name="type" value="config"/>
          <input type="submit" value="Connect"/>
        </form>
        <br/><br/>
      </td>
    </tr>
    <tr>
      <td style="vertical-align:text-top;">Preconfigured CMIS server:</td>
      <td>
        <form action="${url.service}" method="post">
          <select name="server" size="5">
            <#list cmisServers as s>
            <option value="${s.name}">${s.name} (${s.description})</option>
            </#list>
          </select>
          <br/>
          <table>
            <tr><td>Username:</td><td><input type="text" name="username" size="30"/></td></tr>
            <tr><td>Password:</td><td><input type="password" name="password" size="30"/></td></tr>
            <tr><td>Repository Id:</td><td><input type="text" name="repositoryid" size="30"/> (leave empty for the first repository)</td></tr>
          </table>
          <br/>  
          <input type="hidden" name="type" value="server"/>
          <input type="submit" value="Connect"/>
        </form>
        <br/><br/>
      </td>
    </tr>
  </table>
</body>
</html>
