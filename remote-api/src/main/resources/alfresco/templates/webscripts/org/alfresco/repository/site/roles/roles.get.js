function main()
{
   var shortName = url.templateArgs.shortname;
   
   var site = siteService.getSite(shortName);
   if (site === null)
   {
      // Return 404
      status.setCode(404, "Site " + shortName + " does not exist");
      return;
   }
   
   // calculate the available "roles" and permissions groups for this site
   var siteRoles = [];
   var rolesList = siteService.listSiteRoles();
   for (var i in rolesList)
   {
       siteRoles.push(rolesList[i]);
   }
   // add the "None" pseudo role 
   siteRoles.push("None");
   
   var sitePermissionGroups = site.sitePermissionGroups;
   sitePermissionGroups["everyone"] = "GROUP_EVERYONE";
   
   model.siteRoles = siteRoles;
   model.sitePermissionGroups = sitePermissionGroups;
}

main();