function main()
{
	// Get the shortname
	var shortName = url.templateArgs.shortname;
	
	// Get the site
	var site = siteService.getSite(shortName);
	
	if (site == null)
	{
		// Return 404
		status.setCode(404, "Site " + shortName + " does not exist");
		return;
	}
	
	var siteRoles = siteService.listSiteRoles().concat(["None"]);
	var sitePermissionGroups = site.sitePermissionGroups;
	sitePermissionGroups["everyone"] = "GROUP_EVERYONE";
		
	model.siteRoles = siteRoles;
	model.sitePermissionGroups = sitePermissionGroups;
}

main();