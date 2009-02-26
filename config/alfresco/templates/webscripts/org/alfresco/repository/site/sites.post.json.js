function main()
{
	// Get the details of the site
	var shortName = json.get("shortName");
	if (shortName == null || shortName.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Short name missing when creating site");
		return;
	}

    // See if the shortName is available
    var site = siteService.getSite(shortName);
    if (site != null)
    {
        status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "error.duplicateShortName");
        return;
    }

    var sitePreset = json.get("sitePreset");
	if (shortName == null || shortName.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Site preset missing when creating site");
		return;
	}
	
	var title = json.get("title");
	var description = json.get("description");
	
	// Use the visibility flag before the isPublic flag
	var visibility = siteService.PUBLIC_SITE;
    if (json.has("visibility") == true)
    {
        visibility = json.get("visibility");
    }
    else if (json.has("isPublic") == true)
    {
       var isPublic = json.getBoolean("isPublic");
       if (isPublic == true)
       {
          visibility = siteService.PUBLIC_SITE;
       }
       else
       {
          visibility = siteService.PRIVATE_SITE;
       }
    }
	
	// Create the site 
	var site = siteService.createSite(sitePreset, shortName, title, description, visibility);
	
	// Put the created site into the model
	model.site = site;
}

main();	