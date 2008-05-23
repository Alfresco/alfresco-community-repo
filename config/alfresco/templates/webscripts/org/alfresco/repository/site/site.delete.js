function main()
{
	// Get the shortname
	var shortName = url.extension;
	
	// Get the site
	var site = siteService.getSite(shortName);
	if (site != null)
	{
		// Delete the site
		site.deleteSite();
	}
	else
	{
		// Return 404
		status.setCode(404, "The site " + shortName + " does not exist");
		return;
	}
}

main();	