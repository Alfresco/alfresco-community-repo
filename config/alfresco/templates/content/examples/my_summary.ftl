<#-- Table of some summary details about the current user -->
<table>
   <tr><td><b>Name:</b></td> <td>${person.properties.firstName?html} ${person.properties.lastName?html}</td></tr>
   <tr><td><b>User:</b></td> <td>${person.properties.userName}</td></tr>
   <tr><td><b>Home Space location:</b></td> <td>${userhome.displayPath}/${userhome.name}</td></tr>
   <tr><td><b>Items in Home Space:</b></td> <td>${userhome.children?size}</td></tr>
   <tr><td><b>Items in Company Space:</b></td> <td>${companyhome.children?size}</td></tr>
</table>
