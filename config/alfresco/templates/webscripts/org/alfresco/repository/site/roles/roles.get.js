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
   // add the "None" pseudo role 
   var siteRoles = siteService.listSiteRoles().concat(["None"]);
   var sitePermissionGroups = site.sitePermissionGroups;
   sitePermissionGroups["everyone"] = "GROUP_EVERYONE";
   
   model.siteRoles = siteRoles;
   model.sitePermissionGroups = sitePermissionGroups;
}

main();