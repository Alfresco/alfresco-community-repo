<html>
  <head>
    <title>CMIS Server Defintions</title>
    <link rel="stylesheet" type="text/css" href="${url.context}/css/base.css" />
  </head>
  <body>
    <h1>Configured server definitions</h1>
    
    <table>
      <tr>
        <th>Name</td>
        <th>Description</td>
        <th>Parameters</td>      
      </tr>
    <#list cmisServers as s>
      <tr>
        <td>${s.name?html}</td>
        <td>${s.description?html}</td>
        <td> 
          <#assign p = s.parameters>
          <#list p?keys as key>
            <code>${key?html} = ${p[key]?html}</code><br/>
          </#list>
        </td>
      </tr>
    </#list>
    </table>    
  </body>
</html>
