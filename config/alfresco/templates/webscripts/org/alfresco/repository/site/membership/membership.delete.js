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
	try
	{
	   site.removeMembership(userName);
	}
	catch (e)
	{
	   // for a SiteServiceException - ensure it is not wrapped further
	   var msg = e.message;
	   if (msg.indexOf("SiteServiceException") != -1)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, msg);
      }
      else throw error;
	}
}

main();