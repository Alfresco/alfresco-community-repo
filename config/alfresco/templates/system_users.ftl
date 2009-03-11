<h3>System users, company Ids and home space information</h3>
<table cellpadding=2>
   <tr>
      <td><b>Company ID</b></td>
      <td><b>Username</b></td>
      <td><b>Home Space Path</b></td>
      <td><b>Creation Date</b></td>
   </tr>
   <#list space.childrenByXPath["/sys:system/sys:people/*[subtypeOf('cm:person')]"] as child>
         <tr>
            <td>${child.properties["cm:organizationId"]}</td>
            <td>${child.properties["cm:userName"]}</td>
            <td>${child.properties["cm:homeFolder"].displayPath}</td>
            <td>${child.properties["cm:homeFolder"].properties["cm:created"]?date}</td>
         </tr>
   </#list>
</table>