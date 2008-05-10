function main()
{
	// Get the url values
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var userName = urlElements[2];
	
	// Get the site
	var site = siteService.getSite(shortName);
	if (site == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
		return;
	}
	
	// Remove the user from the site
	site.removeMembership(userName);
}

main();