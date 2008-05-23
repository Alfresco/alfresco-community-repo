function main()
{
	// Get the site
	var shortName = url.extension;
	var site = siteService.getSite(shortName);
	
	if (site != null)
	{	
		// Update the sites details
		site.title = json.get("title");
		site.description = json.get("description");
		site.isPublic = json.getBoolean("isPublic");
		site.save();
		
		// Pass the model to the template
		model.site = site;
	}
	else
	{
		// Return 404
		status.setCode(status.STATUS_NOT_FOUND, "Site " + shortName + " does not exist");
		return;
	}
}

main();