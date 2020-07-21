function main()
{
	// Get the details of the site id (site shortname) and/or appTool id
	var siteId = null;
	var appToolId = null;
	
	if (! json.isNull("siteId"))
	{
	   siteId = json.get("siteId");
	}
	
	if (! json.isNull("appToolId"))
    {
        appToolId = json.get("appToolId");
    }
	
	if ((siteId == null || siteId.length == 0) &&
	    (appToolId == null || appToolId.length == 0))
	{
		status.code = 400;
		status.message = "siteId and appToolId are both missing - must supply one or both.";
		status.redirect = true;
		return;
	}
	
	// Set feed control for current user
	activities.setFeedControl(siteId, appToolId);
}

main();	