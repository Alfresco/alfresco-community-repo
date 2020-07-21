function main()
{
	// Get the shortname
	var shortName = url.extension;
	
	// Get the site
	var site = siteService.getSite(shortName);
	if (site == null)
	{
		// Return 404
		status.setCode(404, "The site " + shortName + " does not exist");
		return;
	}
	try
	{
		// Delete the site
		site.deleteSite();
	}
    catch (error)
    {
        var msg = error.message;
        
        // determine if the exception was AlfrescoRuntimeException, if so
        // return 409 status code
        if (msg.indexOf("NodeLockedException") != -1)
        {
            status.setCode(status.STATUS_CONFLICT, msg);
        }
        else
        {
            // We don't need to check for SiteServiceException thrown as a result of
            // not existing site, because this is handled above.
            status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, msg); //return 500
        }
        return;
    }
}

main();