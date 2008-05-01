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
	status.code = 404;
	status.redirect = true;
}	