<#-- Table of some summary details about the current user -->
<table>
   <tr><td><b>${message("templates.my_summary.name")}</b></td> <td>${person.properties.firstName?html} ${person.properties.lastName?html}</td></tr>
   <tr><td><b>${message("templates.my_summary.user")}</b></td> <td>${person.properties.userName}</td></tr>
   <tr><td><b>${message("templates.my_summary.home_space_location")}</b></td> <td>${userhome.displayPath}/${userhome.name}</td></tr>
   <tr><td><b>${message("templates.my_summary.items_in_home_space")}</b></td> <td>${userhome.children?size}</td></tr>
   <tr><td><b>${message("templates.my_summary.items_in_company_space")}</b></td> <td>${companyhome.children?size}</td></tr>
</table>