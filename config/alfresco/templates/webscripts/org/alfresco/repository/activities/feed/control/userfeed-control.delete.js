function main()
{
	// Get the details of the site id (site shortname) and/or appTool id
	var siteId = args["s"];
	var appToolId = args["a"];
	
	if ((siteId == null || siteId.length == 0) &&
	    (appToolId == null || appToolId.length == 0))
	{
		status.code = 400;
		status.message = "siteId and appToolId are both missing - must supply one or both.";
		status.redirect = true;
		return;
	}
	
	// Unset feed control for current user
	activities.unsetFeedControl(siteId, appToolId);
}

main();	