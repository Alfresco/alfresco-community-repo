function main()
{
	// Get the site
	var shortName = url.extension;
	var site = siteService.getSite(shortName);
	
	if (site != null)
	{	
		// Updafte the sites details
		if (json.has("title") == true)
		{
		   site.title = json.get("title");
		}
		if (json.has("description") == true)
		{
		   site.description = json.get("description");
		}
		
		// Use the visibility flag before the isPublic flag
		if (json.has("visibility") == true)
		{
		   site.visibility = json.get("visibility");
		}
		else if (json.has("isPublic") == true)
		{
		   // Deal with deprecated isPublic flag accordingly
		   var isPublic = json.getBoolean("isPublic");
		   if (isPublic == true)
		   {
		      site.visibility = siteService.PUBLIC_SITE;
		   }
		   else
		   {
		      site.visibility = siteService.PRIVATE_SITE;
		   }
	    }
		
		// Save the site
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